package org.ares.cloud.user.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.base.BusinessIdServerClient;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.exception.BusinessException;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.entity.SysRoleEntity;
import org.ares.cloud.user.entity.SysUserRoleEntity;
import org.ares.cloud.user.entity.UserEntity;
import org.ares.cloud.user.repository.SysUserRoleRepository;
import org.ares.cloud.user.service.SysRoleService;
import org.ares.cloud.user.service.SysUserRoleService;
import org.ares.cloud.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户角色关系服务实现类
 */
@Service
public class SysUserRoleServiceImpl extends BaseServiceImpl<SysUserRoleRepository, SysUserRoleEntity> implements SysUserRoleService {

    @Resource
    private BusinessIdServerClient businessIdServerClient;
    @Resource
    private AuthOracleHsmClient authOracleHsmClient;
    @Resource
    private HsmProperties hsmProperties;
    @Resource
    private UserService userService;

    /**
     * 签名算法常量
     */
    private static final String SIGN_ALGORITHM = "SHA256RSA_PKCS1_V15";

    /**
     * 添加用户角色关系
     * @param entity 用户角色实体
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysUserRoleEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getRoleId()) || StringUtils.isBlank(entity.getPhone())) {
            throw new BusinessException("角色和手机号都不得为空");
        }
        if (StringUtils.isBlank(entity.getCountryCode())) {
            entity.setCountryCode("+33");
        }
        // 如果是新增（ID为空），则生成唯一ID并设置创建信息
        // 生成唯一的主键ID
        String id = businessIdServerClient.generateSnowflakeId();
        entity.setId(id);
        // 设置创建者
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setCreator(userId);
        }
        if (StringUtils.isNotBlank(entity.getPhone())) {
            // 查询用户
            LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserEntity::getDeleted, 0);
            wrapper.eq(UserEntity::getCountryCode, entity.getCountryCode());
            wrapper.eq(UserEntity::getPhone, entity.getPhone());
            List<UserEntity> userlist = userService.list(wrapper);
            if (CollectionUtils.isEmpty(userlist)) {
                throw new BusinessException("此手机号还没有注册，请先注册");
            }
            // 校验用户是否已关联角色，一个用户只关联一个角色
            LambdaQueryWrapper<SysUserRoleEntity> userRoleWrapper = new LambdaQueryWrapper<>();
            userRoleWrapper.eq(SysUserRoleEntity::getDeleted, 0);
            userRoleWrapper.eq(SysUserRoleEntity::getUserId, userlist.get(0).getId());
            List<SysUserRoleEntity> userRoleList = this.list(userRoleWrapper);
            if (CollectionUtils.isNotEmpty(userRoleList)) {
                throw new BusinessException("此用户已关联角色，不可再次添加");
            }
            entity.setUserId(userlist.get(0).getId());
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setCreateTime(currentTime);
        SysUserRoleEntity userRoleEntity = changeHsm(entity);
        return super.save(userRoleEntity);
    }

    /**
     * 编辑用户角色关系
     * @param entity 用户角色实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysUserRoleEntity entity) {
        if (entity == null) {
            return false;
        }
        // 设置更新者和更新时间
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setUpdater(userId);
        }
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setUpdateTime(currentTime);
        SysUserRoleEntity userRoleEntity = changeHsm(entity);
        return super.updateById(userRoleEntity);
    }

    /**
     * 删除用户角色关系（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(String id) {
        if (org.ares.cloud.common.utils.StringUtils.isBlank(id)) {
            return false;
        }
        SysUserRoleEntity entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            String userId = ApplicationContext.getUserId();
            if (org.ares.cloud.common.utils.StringUtils.isNotBlank(userId)) {
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
     * 查询用户角色关系分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysUserRoleEntity> pageList(Query query) {
        LambdaQueryWrapper<SysUserRoleEntity> wrapper = getWrapper(query);

        // 添加查询条件：只查询未删除的记录
        wrapper.eq(SysUserRoleEntity::getDeleted, 0);

        // 如果有关键字，可以按用户ID或角色ID搜索
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUserRoleEntity::getUserId, query.getKeyword())
                    .or()
                    .like(SysUserRoleEntity::getRoleId, query.getKeyword()));
        }
        wrapper.orderByAsc(SysUserRoleEntity::getCreateTime);
        IPage<SysUserRoleEntity> page = page(getPage(query), wrapper);
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            List<String> userIdList = page.getRecords().stream().map(SysUserRoleEntity::getUserId).toList();
            if (CollectionUtils.isNotEmpty(userIdList)) {
                // 查询用户信息
                LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(UserEntity::getDeleted, 0);
                userWrapper.in(UserEntity::getId, userIdList);
                List<UserEntity> entityList = userService.list(userWrapper);
                if (CollectionUtils.isNotEmpty(entityList)) {
                    Map<String, UserEntity> map = entityList.stream().collect(Collectors.toMap(UserEntity::getId, v -> v));
                    for (SysUserRoleEntity entity : page.getRecords()) {
                        if (map.containsKey(entity.getUserId())) {
                            if (map.get(entity.getUserId()) != null) {
                                entity.setPhone(map.get(entity.getUserId()).getPhone());
                                entity.setCountryCode(map.get(entity.getUserId()).getCountryCode());
                            }
                        }
                    }
                }
            }
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 根据用户ID获取关联的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表（去重）
     */
    @Override
    public SysUserRoleEntity getRoleIdsByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        LambdaQueryWrapper<SysUserRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRoleEntity::getUserId, userId);
        wrapper.eq(SysUserRoleEntity::getDeleted, 0);
        wrapper.eq(SysUserRoleEntity::getStatus, 1);
        return this.getOne(wrapper);
    }

    /**
     * 构造HSM相关信息
     * @param entity
     * @return
     */
    public SysUserRoleEntity changeHsm(SysUserRoleEntity entity) {
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
     * 哈希内容包含：ID + 关键业务字段(userId) + 关键业务字段(roleId) + VERSION + STATUS + DELETED
     *
     * @param entity 角色实体
     * @return SHA-256哈希值（16进制字符串）
     */
    private String generateDataHash(SysUserRoleEntity entity) {
        try {
            // 构建哈希内容字符串
            StringBuilder hashContent = new StringBuilder();
            hashContent.append(entity.getId() != null ? entity.getId() : "");
            hashContent.append("|");
            hashContent.append(entity.getUserId() != null ? entity.getUserId() : "");
            hashContent.append("|");
            hashContent.append(entity.getRoleId() != null ? entity.getRoleId() : "");
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
     * 签名内容包含：ID + 关键业务字段(userId) + 关键业务字段(roleId) + 时间戳
     *
     * @param entity 角色实体
     * @return HSM签名字符串（Base64编码）
     * @throws Exception 签名失败时抛出异常
     */
    private String generateDataSignature(SysUserRoleEntity entity) throws Exception {
        // 构建签名内容字符串
        StringBuilder signContent = new StringBuilder();
        signContent.append(entity.getId() != null ? entity.getId() : "");
        signContent.append("|");
        signContent.append(entity.getUserId() != null ? entity.getUserId() : "");
        signContent.append("|");
        signContent.append(entity.getRoleId() != null ? entity.getRoleId() : "");
        signContent.append("|");
        signContent.append(entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getCreateTime());
        // 使用Oracle HSM服务进行签名
        OcidHsmDto ocidHsmDto = new OcidHsmDto();
        ocidHsmDto.setData(signContent.toString());
        return authOracleHsmClient.sign(ocidHsmDto);
    }
}
