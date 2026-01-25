package org.ares.cloud.user.service;

import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.database.service.BaseService;
import org.ares.cloud.user.entity.SysClassificationUrlEntity;

import java.io.Serializable;
import java.util.List;

public interface SysClassificationUrlService extends BaseService<SysClassificationUrlEntity> {
    /**
     * 添加分类URL
     * @param entity 分类URL实体
     * @return 是否添加成功
     */
    boolean save(SysClassificationUrlEntity entity);

    /**
     * 编辑分类URL
     * @param entity 分类URL实体
     * @return 是否更新成功
     */
    boolean updateById(SysClassificationUrlEntity entity);

    /**
     * 删除分类URL（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean removeById(Serializable id);

    /**
     * 获取分类下的url
     * @param classificationId
     * @return
     */
    List<SysClassificationUrlDto> queryUrlList(String classificationId);

    /**
     * 根据URL路径查询URL ID
     * @param url URL路径
     * @return URL ID，如果不存在则返回null
     */
    String getUrlIdByUrl(String url);

    /**
     * 查询角色URL关系分页列表
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SysClassificationUrlEntity> pageList(Query query);

    /**
     * 根据角色ID联表查询URL列表
     * @param roleId 角色ID
     * @return URL列表
     */
    List<SysClassificationUrlEntity> selectUrlsByRoleId(String roleId);

}
