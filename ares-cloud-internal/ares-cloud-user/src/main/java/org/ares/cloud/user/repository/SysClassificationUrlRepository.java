package org.ares.cloud.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.ares.cloud.user.entity.SysClassificationUrlEntity;

import java.util.List;

@Mapper
public interface SysClassificationUrlRepository extends BaseMapper<SysClassificationUrlEntity> {

    /**
     * 根据角色ID联表查询URL列表
     * @param roleId 角色ID
     * @return URL列表
     */
    @Select("SELECT cu.* FROM SYS_CLASSIFICATION_URL cu " +
            "INNER JOIN SYS_ROLE_URL ru ON cu.ID = ru.URL_ID " +
            "WHERE ru.ROLE_ID = #{roleId} " +
            "AND ru.DELETED = 0 AND ru.STATUS = 1 " +
            "AND cu.DELETED = 0 AND cu.STATUS = 1")
    List<SysClassificationUrlEntity> selectUrlsByRoleId(@Param("roleId") String roleId);

}
