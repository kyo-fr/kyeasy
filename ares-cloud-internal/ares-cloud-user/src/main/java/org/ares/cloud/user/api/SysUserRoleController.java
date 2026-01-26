package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.user.entity.SysUserRoleEntity;
import org.ares.cloud.user.service.SysUserRoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * @author hugo tangxkwork@163.com
 * @description 用户角色关系 控制器
 * @version 1.0.0
 * @date 2024-10-11
 */
@RestController
@RequestMapping("/api/user/v1/user-roles")
@Tag(name = "用户角色关系")
public class SysUserRoleController {
    @Resource
    private SysUserRoleService sysUserRoleService;

    @GetMapping(value = "/page")
    @Operation(summary = "分页查询用户角色关系列表")
    @RequireUrlPermission
    public Result<PageResult<SysUserRoleEntity>> page(@ParameterObject @Valid Query query) {
        PageResult<SysUserRoleEntity> page = sysUserRoleService.pageList(query);
        return Result.success(page);
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加用户角色关系")
    @RequireUrlPermission
    public Result<String> save(@RequestBody @Valid SysUserRoleEntity entity) {
        sysUserRoleService.save(entity);
        return Result.success();
    }

    @PostMapping(value = "/update")
    @Operation(summary = "编辑用户角色关系")
    @RequireUrlPermission
    public Result<String> update(@RequestBody @Valid SysUserRoleEntity entity) {
        sysUserRoleService.updateById(entity);
        return Result.success();
    }

    @PostMapping(value = "/delete-by-id")
    @Operation(summary = "根据ID删除用户角色关系")
    @RequireUrlPermission
    public Result<String> delete(@RequestBody @Valid SysUserRoleEntity entity) {
        sysUserRoleService.removeById(entity.getId());
        return Result.success();
    }
}

