package org.ares.cloud.user.service;

import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysOperationLogEntity;
import org.ares.cloud.user.query.SysOperationLogQuery;
import org.ares.cloud.common.dto.PageResult;

import java.util.List;

/**
 * @author system
 * @description 操作日志 服务接口
 * @date 2024/12/19
 */
public interface SysOperationLogService extends BaseService<SysOperationLogEntity> {

    /**
     * 创建操作日志
     * @param entity 操作日志实体
     * @return 是否创建成功
     */
    boolean create(SysOperationLogEntity entity);

    /**
     * 批量创建操作日志
     * @param entities 操作日志实体集合
     * @return 是否创建成功
     */
    boolean createBatch(List<SysOperationLogEntity> entities);

    /**
     * 分页查询操作日志列表
     * @param query 查询对象
     * @return 分页结果
     */
    PageResult<SysOperationLogEntity> loadList(SysOperationLogQuery query);
}
