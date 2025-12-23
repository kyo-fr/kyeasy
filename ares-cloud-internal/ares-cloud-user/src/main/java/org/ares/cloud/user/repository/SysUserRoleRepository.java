package org.ares.cloud.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.ares.cloud.user.entity.SysUserRoleEntity;

@Mapper
public interface SysUserRoleRepository extends BaseMapper<SysUserRoleEntity> {
}
