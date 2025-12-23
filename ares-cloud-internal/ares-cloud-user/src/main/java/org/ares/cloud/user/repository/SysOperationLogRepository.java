package org.ares.cloud.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.ares.cloud.user.entity.SysOperationLogEntity;

@Mapper
public interface SysOperationLogRepository extends BaseMapper<SysOperationLogEntity> {
}
