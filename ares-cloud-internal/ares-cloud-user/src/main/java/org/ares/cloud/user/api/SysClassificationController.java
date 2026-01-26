package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.api.user.dto.SysClassificationDto;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.user.entity.SysClassificationEntity;
import org.ares.cloud.user.service.SysClassificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hugo tangxkwork@163.com
 * @description 分类 控制器
 * @version 1.0.0
 * @date 2024-10-11
 */
@RestController
@RequestMapping("/api/user/v1/classifications")
@Tag(name = "分类")
public class SysClassificationController {

    @Resource
    private SysClassificationService sysClassificationService;

    @GetMapping("/page")
    @Operation(summary = "分类树形结构查询")
    @RequireUrlPermission
    public Result<List<SysClassificationDto>> page() {
        List<SysClassificationDto> classificationTree = sysClassificationService.getClassificationTree();
        return Result.success(classificationTree);
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加分类")
    @RequireUrlPermission
    public Result<String> save(@RequestBody @Valid SysClassificationEntity entity) {
        sysClassificationService.save(entity);
        return Result.success();
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "根据ID删除分类")
    @RequireUrlPermission
    public Result<String> delete(@RequestBody @Valid SysClassificationEntity entity) {
        sysClassificationService.removeById(entity.getId());
        return Result.success();
    }
}

