package org.ares.cloud.user.service;

import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysUserRoleEntity;

import java.util.List;

public interface SysUserRoleService extends BaseService<SysUserRoleEntity> {

    /**
     * 添加用户角色关系
     * @param entity 用户角色实体
     * @return 是否添加成功
     */
    boolean save(SysUserRoleEntity entity);

    /**
     * 编辑用户角色关系
     * @param entity 用户角色实体
     * @return 是否更新成功
     */
    boolean updateById(SysUserRoleEntity entity);

    /**
     * 删除用户角色关系（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean removeById(String id);

    /**
     * 查询用户角色关系分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SysUserRoleEntity> pageList(Query query);

    /**
     * 根据用户ID获取关联的角色ID
     * @param userId 用户ID
     * @return 角色ID列表（去重）
     */
    SysUserRoleEntity getRoleIdsByUserId(String userId);

}
