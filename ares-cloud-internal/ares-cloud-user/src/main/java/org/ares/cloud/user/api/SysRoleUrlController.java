package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.api.user.dto.SysRoleUrlDto;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.query.Query;
import org.ares.cloud.user.entity.SysRoleUrlEntity;
import org.ares.cloud.user.service.SysRoleUrlService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hugo tangxkwork@163.com
 * @description 角色URL关系 控制器
 * @version 1.0.0
 * @date 2024-10-11
 */
@RestController
@RequestMapping("/api/user/v1/role-urls")
@Tag(name = "角色URL关系")
public class SysRoleUrlController {
    @Resource
    private SysRoleUrlService sysRoleUrlService;

    @GetMapping(value = "/page")
    @Operation(summary = "分页查询角色URL关系列表")
    @RequireUrlPermission
    public Result<PageResult<SysRoleUrlEntity>> page(@ParameterObject @Valid Query query) {
        PageResult<SysRoleUrlEntity> page = sysRoleUrlService.pageList(query);
        return Result.success(page);
    }

    @PostMapping(value = "/update-url-and-role")
    @Operation(summary = "编辑角色URL关系")
    @RequireUrlPermission
    public Result<String> update(@RequestBody @Valid SysRoleUrlEntity entity) {
        sysRoleUrlService.updateUrlAndRole(entity);
        return Result.success();
    }
}

