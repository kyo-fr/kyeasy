package org.ares.cloud.user.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.base.BusinessIdServerClient;
import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.entity.SysClassificationUrlEntity;
import org.ares.cloud.user.repository.SysClassificationUrlRepository;
import org.ares.cloud.user.service.SysClassificationUrlService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分类URL服务实现类
 */
@Service
public class SysClassificationUrlServiceImpl extends BaseServiceImpl<SysClassificationUrlRepository, SysClassificationUrlEntity> implements SysClassificationUrlService {

    @Resource
    @Lazy
    private BusinessIdServerClient businessIdServerClient;
    @Resource
    private AuthOracleHsmClient authOracleHsmClient;
    @Resource
    private HsmProperties hsmProperties;

    /**
     * 签名算法常量
     */
    private static final String SIGN_ALGORITHM = "SHA256RSA_PKCS1_V15";

    /**
     * 添加分类URL
     * @param entity 分类URL实体
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysClassificationUrlEntity entity) {
        // 生成唯一的主键ID
        String id = businessIdServerClient.generateSnowflakeId();
        entity.setId(id);
        // 设置创建者
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setCreator(userId);
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setCreateTime(currentTime);
        SysClassificationUrlEntity sysClassificationUrlEntity = changeHsm(entity);
        return super.save(sysClassificationUrlEntity);
    }

    /**
     * 编辑分类URL
     * @param entity 分类URL实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysClassificationUrlEntity entity) {
        // 设置更新者和更新时间
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setUpdater(userId);
        }
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setUpdateTime(currentTime);
        SysClassificationUrlEntity sysClassificationUrlEntity = changeHsm(entity);
        return super.updateById(sysClassificationUrlEntity);
    }

    /**
     * 删除分类URL（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        SysClassificationUrlEntity entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            return this.updateById(entity);
        }
        return false;
    }

    @Override
    public List<SysClassificationUrlDto> queryUrlList(String classificationId) {
        if (StringUtils.isBlank(classificationId)) {
            return Collections.EMPTY_LIST;
        }
        List<SysClassificationUrlDto> urlDtoList = new ArrayList<>();
        // 检查是否存在关联的分类URL（classification_id 等于当前 id 且 deleted = 0）
        LambdaQueryWrapper<SysClassificationUrlEntity> urlWrapper = new LambdaQueryWrapper<>();
        urlWrapper.eq(SysClassificationUrlEntity::getClassificationId, classificationId);
        urlWrapper.eq(SysClassificationUrlEntity::getDeleted, 0);
        List<SysClassificationUrlEntity> urlEntityList = this.list(urlWrapper);
        if (CollectionUtils.isNotEmpty(urlEntityList)) {
            for (SysClassificationUrlEntity urlEntity : urlEntityList) {
                SysClassificationUrlDto sysClassificationUrlDto = convertToDto(urlEntity);
                urlDtoList.add(sysClassificationUrlDto);
            }
            return urlDtoList;
        }
        return urlDtoList;
    }

    /**
     * 将实体转换为DTO
     * @param entity 实体
     * @return DTO
     */
    private SysClassificationUrlDto convertToDto(SysClassificationUrlEntity entity) {
        if (entity == null) {
            return null;
        }
        SysClassificationUrlDto dto = new SysClassificationUrlDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 根据URL路径查询URL ID
     * @param url URL路径
     * @return URL ID，如果不存在则返回null
     */
    @Override
    public String getUrlIdByUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        LambdaQueryWrapper<SysClassificationUrlEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationUrlEntity::getUrl, url);
        wrapper.eq(SysClassificationUrlEntity::getDeleted, 0);
        wrapper.eq(SysClassificationUrlEntity::getStatus, 1);
        wrapper.last("LIMIT 1");

        SysClassificationUrlEntity entity = getOne(wrapper);
        return entity != null ? entity.getId() : null;
    }

    /**
     * 构造HSM相关信息
     * @param entity
     * @return
     */
    public SysClassificationUrlEntity changeHsm(SysClassificationUrlEntity entity) {
        entity.setBusinessNo(Long.parseLong(entity.getId()));
        // 生成数据哈希（包含：ID + 关键业务字段 + VERSION + STATUS + DELETED）
        String dataHash = generateDataHash(entity);
        entity.setDataHash(dataHash);

        // 生成数据签名（包含：ID + 关键业务字段 + 时间戳）
        try {
            String dataSignature = generateDataSignature(entity);
            entity.setDataSignature(dataSignature);
            // 设置最后签名时间
            entity.setLastSignTime(entity.getCreateTime());
            // 设置签名算法
            entity.setSignAlgorithm(SIGN_ALGORITHM);
            // 设置公钥ID（从HSM配置中获取）
            if (hsmProperties != null && org.ares.cloud.common.utils.StringUtils.isNotBlank(hsmProperties.getPublicKeyOcid())) {
                entity.setPublicKeyId(hsmProperties.getPublicKeyOcid());
            }
        } catch (Exception e) {
            log.error("HSM签名失败", e);
            throw new RequestBadException("HSM签名失败", e.getMessage());
        }
        return entity;
    }

    /**
     * 生成数据哈希（SHA-256）
     * 哈希内容包含：ID + 关键业务字段(classificationId) + 关键业务字段(url) + VERSION + STATUS + DELETED
     *
     * @param entity 角色实体
     * @return SHA-256哈希值（16进制字符串）
     */
    private String generateDataHash(SysClassificationUrlEntity entity) {
        try {
            // 构建哈希内容字符串
            StringBuilder hashContent = new StringBuilder();
            hashContent.append(entity.getId() != null ? entity.getId() : "");
            hashContent.append("|");
            hashContent.append(entity.getClassificationId() != null ? entity.getClassificationId() : "");
            hashContent.append("|");
            hashContent.append(entity.getUrl() != null ? entity.getUrl() : "");
            hashContent.append("|");
            hashContent.append(entity.getVersion() != null ? entity.getVersion() : "");
            hashContent.append("|");
            hashContent.append(entity.getStatus() != null ? entity.getStatus() : "");
            hashContent.append("|");
            hashContent.append(entity.getDeleted() != null ? entity.getDeleted() : "");

            // 计算SHA-256哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(hashContent.toString().getBytes(StandardCharsets.UTF_8));

            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate data hash", e);
        }
    }

    /**
     * 生成数据签名（HSM签名）
     * 签名内容包含：ID + 关键业务字段(classificationName) + 关键业务字段(url) + 时间戳
     *
     * @param entity 角色实体
     * @return HSM签名字符串（Base64编码）
     * @throws Exception 签名失败时抛出异常
     */
    private String generateDataSignature(SysClassificationUrlEntity entity) throws Exception {
        // 构建签名内容字符串
        StringBuilder signContent = new StringBuilder();
        signContent.append(entity.getId() != null ? entity.getId() : "");
        signContent.append("|");
        signContent.append(entity.getClassificationId() != null ? entity.getClassificationId() : "");
        signContent.append("|");
        signContent.append(entity.getUrl() != null ? entity.getUrl() : "");
        signContent.append("|");
        signContent.append(entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getCreateTime());
        // 使用Oracle HSM服务进行签名
        OcidHsmDto ocidHsmDto = new OcidHsmDto();
        ocidHsmDto.setData(signContent.toString());
        return authOracleHsmClient.sign(ocidHsmDto);
    }
}
