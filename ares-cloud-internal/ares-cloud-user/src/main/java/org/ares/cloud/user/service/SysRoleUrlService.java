package org.ares.cloud.user.service;

import org.ares.cloud.api.user.dto.SysRoleUrlDto;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysRoleUrlEntity;

import java.io.Serializable;
import java.util.List;

public interface SysRoleUrlService extends BaseService<SysRoleUrlEntity> {
    /**
     * 添加角色URL关系
     * @param entity 角色URL实体
     * @return 是否添加成功
     */
    boolean save(SysRoleUrlEntity entity);

    /**
     * 编辑角色URL关系
     * @param entity 角色URL实体
     * @return 是否更新成功
     */
    boolean updateById(SysRoleUrlEntity entity);

    /**
     * 删除角色URL关系（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean removeById(Serializable id);

    /**
     * 查询角色URL关系分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SysRoleUrlEntity> pageList(Query query);

    /**
     * 根据URL ID集合获取关联的角色ID列表
     * @param entity
     * @return
     */
    List<SysRoleUrlDto> getRoleIdsByUrlIds(SysRoleUrlEntity entity);

    /**
     * 编辑URL和角色的关系
     * @param entity 角色URL实体
     * @return 是否更新成功
     */
    boolean updateUrlAndRole(SysRoleUrlEntity entity);
}
