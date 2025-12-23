package org.ares.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.utils.DateUtils;
import org.ares.cloud.database.service.impl.BaseServiceImpl;
import org.ares.cloud.user.entity.SysOperationLogEntity;
import org.ares.cloud.user.query.SysOperationLogQuery;
import org.ares.cloud.user.repository.SysOperationLogRepository;
import org.ares.cloud.user.service.SysOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author system
 * @description 操作日志 服务实现类
 * @date 2024/12/19
 */
@Service
public class SysOperationLogServiceImpl extends BaseServiceImpl<SysOperationLogRepository, SysOperationLogEntity> implements SysOperationLogService {

    /**
     * 创建操作日志
     * @param entity 操作日志实体
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysOperationLogEntity entity) {
        if (entity == null) {
            return false;
        }
        // 设置创建者
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            entity.setCreator(userId);
        }
        // 设置创建时间
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        entity.setCreateTime(currentTime);
        // 如果删除标记为空，设置为0（未删除）
        if (entity.getDeleted() == null) {
            entity.setDeleted(0);
        }
        // 如果版本号为空，设置为0
        if (entity.getVersion() == null) {
            entity.setVersion(0);
        }
        return super.save(entity);
    }

    /**
     * 批量创建操作日志
     * @param entities 操作日志实体集合
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatch(List<SysOperationLogEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        // 设置创建者和创建时间
        String userId = ApplicationContext.getUserId();
        long currentTime = DateUtils.getCurrentTimestampInUTC();
        for (SysOperationLogEntity entity : entities) {
            if (StringUtils.isNotBlank(userId)) {
                entity.setCreator(userId);
            }
            entity.setCreateTime(currentTime);
            if (entity.getDeleted() == null) {
                entity.setDeleted(0);
            }
            if (entity.getVersion() == null) {
                entity.setVersion(0);
            }
        }
        return super.saveBatch(entities);
    }

    /**
     * 分页查询操作日志列表
     * @param query 查询对象
     * @return 分页结果
     */
    @Override
    public PageResult<SysOperationLogEntity> loadList(SysOperationLogQuery query) {
        IPage<SysOperationLogEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 构建查询条件
     * @param query 查询对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysOperationLogEntity> getWrapper(SysOperationLogQuery query) {
        LambdaQueryWrapper<SysOperationLogEntity> wrapper = super.getWrapper(query);
        // 只查询未删除的数据
        wrapper.eq(SysOperationLogEntity::getDeleted, 0);
        // 根据用户ID查询
        if (query.getUserId() != null) {
            wrapper.eq(SysOperationLogEntity::getUserId, query.getUserId());
        }
        // 根据角色ID查询
        if (query.getRoleId() != null) {
            wrapper.eq(SysOperationLogEntity::getRoleId, query.getRoleId());
        }
        // 根据状态查询
        if (query.getStatus() != null) {
            wrapper.eq(SysOperationLogEntity::getStatus, query.getStatus());
        }
        // 根据URL模糊查询
        if (StringUtils.isNotBlank(query.getUrl())) {
            wrapper.like(SysOperationLogEntity::getUrl, query.getUrl());
        }
        // 根据请求方法查询
        if (StringUtils.isNotBlank(query.getRequestMethod())) {
            wrapper.eq(SysOperationLogEntity::getRequestMethod, query.getRequestMethod());
        }
        // 处理开始时间
        if (query.getStartTime() != null) {
            wrapper.ge(SysOperationLogEntity::getCreateTime, query.getStartTime());
        }
        // 处理结束时间
        if (query.getEndTime() != null) {
            wrapper.le(SysOperationLogEntity::getCreateTime, query.getEndTime());
        }
        // 关键字搜索
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w ->
                    w.like(SysOperationLogEntity::getUrl, query.getKeyword())
                            .or()
                            .like(SysOperationLogEntity::getIpAddress, query.getKeyword())
                            .or()
                            .like(SysOperationLogEntity::getTraceId, query.getKeyword())
            );
        }
        return wrapper;
    }
}
