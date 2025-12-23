package org.ares.cloud.user.service;

import org.ares.cloud.api.user.dto.SysClassificationDto;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysClassificationEntity;

import java.util.List;

public interface SysClassificationService extends BaseService<SysClassificationEntity> {
    /**
     * 添加分类
     * @param entity 分类实体
     * @return 是否添加成功
     */
    boolean save(SysClassificationEntity entity);

    /**
     * 编辑分类
     * @param entity 分类实体
     * @return 是否更新成功
     */
    boolean updateById(SysClassificationEntity entity);

    /**
     * 删除分类（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean removeById(String id);

    /**
     * 查询分类树形列表
     * @return SysClassificationDto
     */
    List<SysClassificationDto> getClassificationTree();
}
