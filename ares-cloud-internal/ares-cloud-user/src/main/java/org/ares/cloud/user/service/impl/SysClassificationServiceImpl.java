package org.ares.cloud.user.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.base.BusinessIdServerClient;
import org.ares.cloud.api.user.dto.SysClassificationDto;
import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.entity.SysClassificationEntity;
import org.ares.cloud.user.repository.SysClassificationRepository;
import org.ares.cloud.user.service.SysClassificationService;
import org.ares.cloud.user.service.SysClassificationUrlService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类服务实现类
 */
@Service
public class SysClassificationServiceImpl extends BaseServiceImpl<SysClassificationRepository, SysClassificationEntity> implements SysClassificationService {

    @Resource
    @Lazy
    private BusinessIdServerClient businessIdServerClient;
    @Resource
    private SysClassificationUrlService sysClassificationUrlService;
    @Resource
    private AuthOracleHsmClient authOracleHsmClient;
    @Resource
    private HsmProperties hsmProperties;

    /**
     * 签名算法常量
     */
    private static final String SIGN_ALGORITHM = "SHA256RSA_PKCS1_V15";

    /**
     * 添加分类
     * @param entity 分类实体
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysClassificationEntity entity) {
        // 如果是新增（ID为空），则生成唯一ID并设置创建信息
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
        SysClassificationEntity sysClassificationEntity = changeHsm(entity);
        return super.save(sysClassificationEntity);
    }

    /**
     * 编辑分类
     * @param entity 分类实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysClassificationEntity entity) {
        // 设置更新者和更新时间
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setUpdater(userId);
        }
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setUpdateTime(currentTime);
        SysClassificationEntity sysClassificationEntity = changeHsm(entity);
        return super.updateById(sysClassificationEntity);
    }

    /**
     * 删除分类（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(String id) {
        SysClassificationEntity entity = this.getById(id);
        if (entity == null) {
            return false;
        }
        // 检查是否存在子类（parent_id 等于当前 id 的记录）
        LambdaQueryWrapper<SysClassificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationEntity::getParentId, id);
        wrapper.eq(SysClassificationEntity::getDeleted, 0); // 只查询未删除的子类
        long childCount = this.count(wrapper);
        // 如果存在子类，不允许删除
        if (childCount > 0) {
            throw new RequestBadException("该分类下存在子分类，无法删除");
        }
        List<SysClassificationUrlDto> urlDtoList = sysClassificationUrlService.queryUrlList(id);
        // 如果存在关联的分类URL，不允许删除
        if (CollectionUtils.isNotEmpty(urlDtoList)) {
            throw new RequestBadException("该分类下存在关联的URL，无法删除");
        }
        // 执行逻辑删除
        entity.setDeleted(1);
        return this.updateById(entity);
    }

    /**
     * 查询分类树形列表
     * @return 分类树形列表
     */
    @Override
    public List<SysClassificationDto> getClassificationTree() {
        // 构建查询条件
        LambdaQueryWrapper<SysClassificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationEntity::getDeleted, 0); // 只查询未删除的数据
        wrapper.eq(SysClassificationEntity::getParentId, "0");
        // 查询所有根节点
        List<SysClassificationEntity> rootEntities = this.list(wrapper);
        if (rootEntities.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询所有数据用于构建树
        LambdaQueryWrapper<SysClassificationEntity> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(SysClassificationEntity::getDeleted, 0);
        List<SysClassificationEntity> allEntities = this.list(allWrapper);
        // 构建树形结构
        List<SysClassificationDto> result = new ArrayList<>();
        for (SysClassificationEntity rootEntity : rootEntities) {
            SysClassificationDto rootDto = convertToDto(rootEntity);
            List<SysClassificationUrlDto> urlDtoList = sysClassificationUrlService.queryUrlList(rootEntity.getId());
            rootDto.setUrlList(urlDtoList);
            buildTree(rootDto, allEntities);
            result.add(rootDto);
        }

        return result;
    }

    /**
     * 将实体转换为DTO
     * @param entity 实体
     * @return DTO
     */
    private SysClassificationDto convertToDto(SysClassificationEntity entity) {
        if (entity == null) {
            return null;
        }
        SysClassificationDto dto = new SysClassificationDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 递归构建树形结构
     * @param parentDto 父节点DTO
     * @param allEntities 所有实体列表
     */
    private void buildTree(SysClassificationDto parentDto, List<SysClassificationEntity> allEntities) {
        if (parentDto == null || allEntities == null || allEntities.isEmpty()) {
            return;
        }
        // 查询所有parentId等于当前节点id的子节点
        List<SysClassificationEntity> childrenEntities = new ArrayList<>();
        for (SysClassificationEntity entity : allEntities) {
            if (StringUtils.isNotBlank(entity.getParentId())
                    && entity.getParentId().equals(parentDto.getId())) {
                childrenEntities.add(entity);
            }
        }
        // 如果没有子节点，直接返回
        if (childrenEntities.isEmpty()) {
            return;
        }
        // 转换子节点为DTO并递归构建子树
        List<SysClassificationDto> childrenDtos = new ArrayList<>();
        for (SysClassificationEntity childEntity : childrenEntities) {
            SysClassificationDto childDto = convertToDto(childEntity);
            List<SysClassificationUrlDto> urlDtoList = sysClassificationUrlService.queryUrlList(childEntity.getId());
            childDto.setUrlList(urlDtoList);
            buildTree(childDto, allEntities); // 递归查询子节点的子节点
            childrenDtos.add(childDto);
        }
        parentDto.setChildren(childrenDtos);
    }

    /**
     * 构造HSM相关信息
     * @param entity
     * @return
     */
    public SysClassificationEntity changeHsm(SysClassificationEntity entity) {
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
     * 哈希内容包含：ID + 关键业务字段(classificationName) + VERSION + STATUS + DELETED
     *
     * @param entity 角色实体
     * @return SHA-256哈希值（16进制字符串）
     */
    private String generateDataHash(SysClassificationEntity entity) {
        try {
            // 构建哈希内容字符串
            StringBuilder hashContent = new StringBuilder();
            hashContent.append(entity.getId() != null ? entity.getId() : "");
            hashContent.append("|");
            hashContent.append(entity.getClassificationName() != null ? entity.getClassificationName() : "");
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
     * 签名内容包含：ID + 关键业务字段(classificationName) + 时间戳
     *
     * @param entity 角色实体
     * @return HSM签名字符串（Base64编码）
     * @throws Exception 签名失败时抛出异常
     */
    private String generateDataSignature(SysClassificationEntity entity) throws Exception {
        // 构建签名内容字符串
        StringBuilder signContent = new StringBuilder();
        signContent.append(entity.getId() != null ? entity.getId() : "");
        signContent.append("|");
        signContent.append(entity.getClassificationName() != null ? entity.getClassificationName() : "");
        signContent.append("|");
        signContent.append(entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getCreateTime());
        // 使用Oracle HSM服务进行签名
        OcidHsmDto ocidHsmDto = new OcidHsmDto();
        ocidHsmDto.setData(signContent.toString());
        return authOracleHsmClient.sign(ocidHsmDto);
    }
}
