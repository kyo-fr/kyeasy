package org.ares.cloud.user.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.base.BusinessIdServerClient;
import org.ares.cloud.api.user.dto.SysRoleUrlDto;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.entity.SysClassificationUrlEntity;
import org.ares.cloud.user.entity.SysRoleEntity;
import org.ares.cloud.user.entity.SysRoleUrlEntity;
import org.ares.cloud.user.repository.SysRoleUrlRepository;
import org.ares.cloud.user.service.SysClassificationUrlService;
import org.ares.cloud.user.service.SysRoleService;
import org.ares.cloud.user.service.SysRoleUrlService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色URL关系服务实现类
 */
@Service
public class SysRoleUrlServiceImpl extends BaseServiceImpl<SysRoleUrlRepository, SysRoleUrlEntity> implements SysRoleUrlService {

    @Resource
    private BusinessIdServerClient businessIdServerClient;
    @Resource
    private SysRoleService sysRoleService;
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
     * 添加角色URL关系
     * @param entity 角色URL实体
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysRoleUrlEntity entity) {
        // 生成唯一的主键ID
        String id = businessIdServerClient.generateSnowflakeId();
        entity.setId(id);
        // 设置创建者
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setCreator(userId);
        }
        if (StringUtils.isNotBlank(entity.getRoleId())) {
            SysRoleEntity roleEntity = sysRoleService.getById(entity.getRoleId());
            if (roleEntity != null) {
                entity.setRoleName(roleEntity.getRoleName());
            }
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setCreateTime(currentTime);
        SysRoleUrlEntity roleUrlEntity = changeHsm(entity);
        return super.save(roleUrlEntity);
    }

    /**
     * 编辑角色URL关系
     * @param entity 角色URL实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysRoleUrlEntity entity) {
        // 设置更新者和更新时间
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setUpdater(userId);
        }
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setUpdateTime(currentTime);
        SysRoleUrlEntity roleUrlEntity = changeHsm(entity);
        return super.updateById(roleUrlEntity);
    }

    /**
     * 删除角色URL关系（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        SysRoleUrlEntity entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            return this.updateById(entity);
        }
        return false;
    }

    /**
     * 查询角色URL关系分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysRoleUrlEntity> pageList(Query query) {
        List<String> urlIdList = new ArrayList<>();
        if (StringUtils.isNoneBlank(query.getKeyword())) {
            LambdaQueryWrapper<SysClassificationUrlEntity> urlWrapper = new LambdaQueryWrapper<>();
            urlWrapper.eq(SysClassificationUrlEntity::getDeleted, 0);
            urlWrapper.like(SysClassificationUrlEntity::getUrl, query.getKeyword());
            List<SysClassificationUrlEntity> urlEntityList = sysClassificationUrlService.list(urlWrapper);
            if (CollectionUtils.isNotEmpty(urlEntityList)) {
                urlIdList = urlEntityList.stream().map(SysClassificationUrlEntity::getId).toList();
            }
        }
        // 第一步：构建查询条件，只查询 urlId 字段以减少数据传输
        LambdaQueryWrapper<SysRoleUrlEntity> wrapper = getWrapper(query);
        wrapper.eq(SysRoleUrlEntity::getDeleted, 0);
        // 如果有关键字，可以按角色ID或URL ID搜索
        if (CollectionUtils.isNotEmpty(urlIdList)) {
            List<String> finalUrlIdList = urlIdList;
            wrapper.and(w -> w.in(SysRoleUrlEntity::getUrlId, finalUrlIdList));
        }
        // 只查询 urlId 字段，减少数据传输
        wrapper.select(SysRoleUrlEntity::getUrlId);

        // 查询所有符合条件的 urlId（只查询 urlId 字段，减少数据传输）
        List<SysRoleUrlEntity> urlIdRecords = list(wrapper);
        if (CollectionUtils.isEmpty(urlIdRecords)) {
            return new PageResult<>(List.of(), 0L);
        }

        // 获取所有不重复的 urlId
        List<String> distinctUrlIds = urlIdRecords.stream()
                .map(SysRoleUrlEntity::getUrlId)
                .distinct()
                .collect(Collectors.toList());

        // 第二步：手动分页 urlId 列表
        int page = query.getPage();
        int limit = query.getLimit();
        int total = distinctUrlIds.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<String> pagedUrlIds = start < total ? distinctUrlIds.subList(start, end) : List.of();

        if (CollectionUtils.isEmpty(pagedUrlIds)) {
            return new PageResult<>(List.of(), (long) total);
        }

        // 第三步：批量查询分页后的 urlId 对应的所有 roleId 和 roleName
        LambdaQueryWrapper<SysRoleUrlEntity> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(SysRoleUrlEntity::getDeleted, 0);
        detailWrapper.in(SysRoleUrlEntity::getUrlId, pagedUrlIds);
        // 只查询需要的字段：urlId, roleId, roleName
        detailWrapper.select(SysRoleUrlEntity::getUrlId, SysRoleUrlEntity::getRoleId, SysRoleUrlEntity::getRoleName);
        List<SysRoleUrlEntity> detailRecords = list(detailWrapper);

        // 第四步：按 urlId 分组，组装 roleList
        Map<String, List<SysRoleUrlEntity>> urlIdGroupMap = detailRecords.stream()
                .collect(Collectors.groupingBy(SysRoleUrlEntity::getUrlId));

        // 第五步：查询 urlName
        LambdaQueryWrapper<SysClassificationUrlEntity> classUrlWrapper = new LambdaQueryWrapper<>();
        classUrlWrapper.eq(SysClassificationUrlEntity::getDeleted, 0);
        classUrlWrapper.in(SysClassificationUrlEntity::getId, pagedUrlIds);
        List<SysClassificationUrlEntity> urlEntityList = sysClassificationUrlService.list(classUrlWrapper);
        Map<String, String> urlNameMap = urlEntityList.stream()
                .collect(Collectors.toMap(SysClassificationUrlEntity::getId, SysClassificationUrlEntity::getUrl));

        // 第六步：组装结果
        List<SysRoleUrlEntity> resultList = new ArrayList<>();
        for (String urlId : pagedUrlIds) {
            SysRoleUrlEntity entity = new SysRoleUrlEntity();
            entity.setUrlId(urlId);

            // 设置 urlName
            if (urlNameMap.containsKey(urlId)) {
                entity.setUrlName(urlNameMap.get(urlId));
            }

            // 设置 roleList
            if (urlIdGroupMap.containsKey(urlId)) {
                List<SysRoleUrlEntity> roleUrlEntities = urlIdGroupMap.get(urlId);
                List<SysRoleEntity> roleList = new ArrayList<>();
                for (SysRoleUrlEntity roleUrlEntity : roleUrlEntities) {
                    SysRoleEntity roleEntity = new SysRoleEntity();
                    roleEntity.setId(roleUrlEntity.getRoleId());
                    roleEntity.setRoleName(roleUrlEntity.getRoleName());
                    roleList.add(roleEntity);
                }
                entity.setRoleList(roleList);
            }

            resultList.add(entity);
        }

        return new PageResult<>(resultList, (long) total);
    }

    @Override
    public List<SysRoleUrlDto> getRoleIdsByUrlIds(SysRoleUrlEntity entity) {
        if (entity == null || CollectionUtils.isEmpty(entity.getUrlList())) {
            return List.of();
        }
        LambdaQueryWrapper<SysRoleUrlEntity> wrapper = new LambdaQueryWrapper<>();
        // 查询指定URL ID集合的记录
        wrapper.in(SysRoleUrlEntity::getUrlId, entity.getUrlList());
        // 只查询未删除的记录
        wrapper.eq(SysRoleUrlEntity::getDeleted, 0);
        List<SysRoleUrlEntity> entities = list(wrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return List.of();
        }
        List<SysRoleUrlDto> sysRoleUrlDtoList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Map<String, List<SysRoleUrlEntity>> collectMap = entities.stream().collect(Collectors.groupingBy(SysRoleUrlEntity::getUrlId));
        for (SysRoleUrlEntity roleUrlEntity : entities) {
            if (!map.containsKey(roleUrlEntity.getUrlId())) {
                map.put(roleUrlEntity.getUrlId(), null);
                SysRoleUrlDto sysRoleUrlDto = convertToDto(roleUrlEntity);
                if (collectMap.containsKey(roleUrlEntity.getUrlId())) {
                    List<SysRoleUrlEntity> sysRoleUrlEntities = collectMap.get(roleUrlEntity.getUrlId());
                    if (CollectionUtils.isNotEmpty(sysRoleUrlEntities)) {
                        List<SysRoleUrlDto.RoleDto> roleGroupList = new ArrayList<>();
                        for (SysRoleUrlEntity sysRoleUrlEntity1 : sysRoleUrlEntities) {
                            SysRoleUrlDto.RoleDto roleDto = new SysRoleUrlDto.RoleDto();
                            roleDto.setRoleId(sysRoleUrlEntity1.getRoleId());
                            roleDto.setRoleName(sysRoleUrlEntity1.getRoleName());
                            roleGroupList.add(roleDto);
                        }
                        sysRoleUrlDto.setRoleDtoList(roleGroupList);
                    }
                }
                sysRoleUrlDtoList.add(sysRoleUrlDto);
            }
        }
        return sysRoleUrlDtoList;

    }

    @Override
    public boolean updateUrlAndRole(SysRoleUrlEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getUrlId())) {
            return true;
        }
        // 查询指定url的角色
        LambdaQueryWrapper<SysRoleUrlEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleUrlEntity::getUrlId, entity.getUrlId());
        wrapper.eq(SysRoleUrlEntity::getDeleted, 0);
        List<SysRoleUrlEntity> entities = this.list(wrapper);
        if (CollectionUtils.isEmpty(entities)) {
            // 表中数据为空
            if (CollectionUtils.isEmpty(entity.getRoleIdList())) {
                // 如果入参角色为空，直接返回
                return true;
            } else {
                // 入参数据不为空，则新增入参中的角色
                for (String roleId : entity.getRoleIdList()) {
                    SysRoleUrlEntity newEntity = new SysRoleUrlEntity();
                    newEntity.setUrlId(entity.getUrlId());
                    newEntity.setRoleId(roleId);
                    this.save(newEntity);
                }
            }
        } else {
            // 如果表中不为空
            if (CollectionUtils.isEmpty(entity.getRoleIdList())) {
                // 入参角色为空，则全部删除表中此url关联的角色
                for (SysRoleUrlEntity sysRoleUrlEntity : entities) {
                    this.removeById(sysRoleUrlEntity.getId());
                }
            } else {
                // 入参角色不为空，对比表中数据跟入参数据
                Map<String, String> dataMap = entities.stream().collect(Collectors.toMap(SysRoleUrlEntity::getRoleId, SysRoleUrlEntity::getRoleId));
                // 遍历入参的roleId
                for (String roleId : entity.getRoleIdList()) {
                    if (!dataMap.containsKey(roleId)) {
                        // 表中不存在入参的角色ID，则新增
                        SysRoleUrlEntity newEntity = new SysRoleUrlEntity();
                        newEntity.setUrlId(entity.getUrlId());
                        newEntity.setRoleId(roleId);
                        this.save(newEntity);
                    }
                }
                // 遍历表中的数据
                for (SysRoleUrlEntity sysRoleUrlEntity : entities) {
                    // 入参中不存在的角色，则删除
                    if (!entity.getRoleIdList().contains(sysRoleUrlEntity.getRoleId())) {
                        this.removeById(sysRoleUrlEntity.getId());
                    }
                }
            }
        }
        return true;
    }

    /**
     * 将实体转换为DTO
     * @param entity 实体
     * @return DTO
     */
    private SysRoleUrlDto convertToDto(SysRoleUrlEntity entity) {
        if (entity == null) {
            return null;
        }
        SysRoleUrlDto dto = new SysRoleUrlDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 构造HSM相关信息
     * @param entity
     * @return
     */
    public SysRoleUrlEntity changeHsm(SysRoleUrlEntity entity) {
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
     * 哈希内容包含：ID + 关键业务字段(urlId) + 关键业务字段(roleId) + VERSION + STATUS + DELETED
     *
     * @param entity 角色实体
     * @return SHA-256哈希值（16进制字符串）
     */
    private String generateDataHash(SysRoleUrlEntity entity) {
        try {
            // 构建哈希内容字符串
            StringBuilder hashContent = new StringBuilder();
            hashContent.append(entity.getId() != null ? entity.getId() : "");
            hashContent.append("|");
            hashContent.append(entity.getUrlId() != null ? entity.getUrlId() : "");
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
     * 签名内容包含：ID + 关键业务字段(urlId) + 关键业务字段(roleId) + 时间戳
     *
     * @param entity 角色实体
     * @return HSM签名字符串（Base64编码）
     * @throws Exception 签名失败时抛出异常
     */
    private String generateDataSignature(SysRoleUrlEntity entity) throws Exception {
        // 构建签名内容字符串
        StringBuilder signContent = new StringBuilder();
        signContent.append(entity.getId() != null ? entity.getId() : "");
        signContent.append("|");
        signContent.append(entity.getUrlId() != null ? entity.getUrlId() : "");
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
