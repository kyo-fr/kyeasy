package org.ares.cloud.user.service;

import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysRoleEntity;

public interface SysRoleService extends BaseService<SysRoleEntity> {

    /**
     * 删除角色
     * @param id
     * @return
     */
    boolean removeById(String id);

    /**
     * 查询角色分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SysRoleEntity> pageList(Query query);
}
