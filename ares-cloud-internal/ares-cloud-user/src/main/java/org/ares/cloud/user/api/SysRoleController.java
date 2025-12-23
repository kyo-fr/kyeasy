package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.user.entity.SysRoleEntity;
import org.ares.cloud.user.service.SysRoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * @author hugo tangxkwork@163.com
 * @description 角色 控制器
 * @version 1.0.0
 * @date 2025-12-05
 */
@RestController
@RequestMapping("/api/user/v1/roles")
@Tag(name = "角色")
public class SysRoleController {
    @Resource
    private SysRoleService sysRoleService;

    @GetMapping(value = "/page")
    @Operation(summary = "分页查询角色列表")
    public Result<PageResult<SysRoleEntity>> page(@ParameterObject @Valid Query query) {
        PageResult<SysRoleEntity> page = sysRoleService.pageList(query);
        return Result.success(page);
    }

    @PostMapping(value = "/add-role")
    @Operation(summary = "添加角色")
    public Result<String> save(@RequestBody @Valid SysRoleEntity entity) {
        sysRoleService.save(entity);
        return Result.success();
    }

    @PostMapping("/delete-role")
    @Operation(summary = "根据ID删除角色")
    public Result<String> delete(@RequestBody SysRoleEntity entity) {
        sysRoleService.removeById(entity.getId());
        return Result.success();
    }
}

