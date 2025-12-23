package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.user.entity.SysOperationLogEntity;
import org.ares.cloud.user.query.SysOperationLogQuery;
import org.ares.cloud.user.service.SysOperationLogService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author system
 * @description 操作日志 控制器
 * @date 2024/12/19
 */
@RestController
@RequestMapping("/api/user/v1/operation-logs")
@Tag(name = "操作日志")
public class SysOperationLogController {

    @Resource
    private SysOperationLogService sysOperationLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询操作日志列表")
    public Result<PageResult<SysOperationLogEntity>> page(@ParameterObject @Valid SysOperationLogQuery query) {
        PageResult<SysOperationLogEntity> page = sysOperationLogService.loadList(query);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建操作日志")
    public Result<String> create(@RequestBody @Valid SysOperationLogEntity entity) {
        sysOperationLogService.create(entity);
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建操作日志")
    public Result<String> createBatch(@RequestBody @Valid List<SysOperationLogEntity> entities) {
        sysOperationLogService.createBatch(entities);
        return Result.success();
    }

}

