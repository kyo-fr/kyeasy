package org.ares.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.base.BusinessIdServerClient;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.common.utils.StringUtils;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.entity.SysRoleEntity;
import org.ares.cloud.user.repository.SysRoleRepository;
import org.ares.cloud.user.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class SysRoleServiceImpl extends BaseServiceImpl<SysRoleRepository, SysRoleEntity> implements SysRoleService {

    @Resource
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
     * 添加单个角色
     * @param entity 角色实体
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysRoleEntity entity) {
        if (entity == null) {
            return false;
        }
        // 生成唯一的角色ID
        String roleId = businessIdServerClient.generateSnowflakeId();
        if (StringUtils.isBlank(roleId)) {
            throw new RequestBadException("获取雪花ID失败");
        }
        entity.setId(roleId);
        // 设置创建者
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setCreator(userId);
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setCreateTime(currentTime);
        // 设置默认值
        if (entity.getStatus() == null) {
            entity.setStatus(1); // 默认状态为正常
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(0); // 默认未删除
        }
        if (entity.getVersion() == null) {
            entity.setVersion(0); // 默认版本号为0
        }

        SysRoleEntity sysRoleEntity = changeHsm(entity);
        return super.save(sysRoleEntity);
    }

    /**
     * 编辑角色
     * @param entity 角色实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysRoleEntity entity) {
        if (entity == null) {
            return false;
        }
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setUpdater(userId);
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setUpdateTime(currentTime);
        SysRoleEntity sysRoleEntity = changeHsm(entity);
        return super.updateById(sysRoleEntity);
    }

    /**
     * 删除角色（逻辑删除）
     * @param id 角色ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        SysRoleEntity entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            String userId = ApplicationContext.getUserId();
            if (StringUtils.isNotBlank(userId)) {
                entity.setUpdater(userId);
            }
            // 设置创建时间
            long currentTime = DateUtils.getCurrentTimestampInUTC();
            entity.setUpdateTime(currentTime);
            return this.updateById(entity);
        }
        return false;
    }

    /**
     * 查询角色分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysRoleEntity> pageList(Query query) {
        LambdaQueryWrapper<SysRoleEntity> wrapper = getWrapper(query);
        // 添加查询条件：只查询未删除的记录
        wrapper.eq(SysRoleEntity::getDeleted, 0);
        wrapper.orderByAsc(SysRoleEntity::getCreateTime);
        IPage<SysRoleEntity> page = page(getPage(query), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 构造HSM相关信息
     * @param entity
     * @return
     */
    public SysRoleEntity changeHsm(SysRoleEntity entity) {
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
            if (hsmProperties != null && StringUtils.isNotBlank(hsmProperties.getPublicKeyOcid())) {
                entity.setPublicKeyId(hsmProperties.getPublicKeyOcid());
            }
        } catch (RequestBadException e) {
            // 如果是参数验证异常，直接抛出
            log.error("HSM签名失败：参数验证错误", e);
            throw e;
        } catch (Exception e) {
            // 其他异常，包装为 RequestBadException
            log.error("HSM签名失败", e);
            throw new RequestBadException(e.getMessage());
        }
        return entity;
    }

    /**
     * 生成数据哈希（SHA-256）
     * 哈希内容包含：ID + 关键业务字段(roleName) + VERSION + STATUS + DELETED
     *
     * @param entity 角色实体
     * @return SHA-256哈希值（16进制字符串）
     */
    private String generateDataHash(SysRoleEntity entity) {
        try {
            // 构建哈希内容字符串
            StringBuilder hashContent = new StringBuilder();
            hashContent.append(entity.getId() != null ? entity.getId() : "");
            hashContent.append("|");
            hashContent.append(entity.getRoleName() != null ? entity.getRoleName() : "");
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
     * 签名内容包含：ID + 关键业务字段(roleName) + 时间戳
     *
     * @param entity 角色实体
     * @return HSM签名字符串（Base64编码）
     * @throws Exception 签名失败时抛出异常
     */
    private String generateDataSignature(SysRoleEntity entity) throws Exception {
        // 构建签名内容字符串
        StringBuilder signContent = new StringBuilder();
        signContent.append(entity.getId() != null ? entity.getId() : "");
        signContent.append("|");
        signContent.append(entity.getRoleName() != null ? entity.getRoleName() : "");
        signContent.append("|");
        signContent.append(entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getCreateTime());
        // 使用Oracle HSM服务进行签名
        OcidHsmDto ocidHsmDto = new OcidHsmDto();
        ocidHsmDto.setData(signContent.toString());
        return authOracleHsmClient.sign(ocidHsmDto);
    }

}
