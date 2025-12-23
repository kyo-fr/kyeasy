package org.ares.cloud.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.user.entity.SysClassificationUrlEntity;
import org.ares.cloud.user.service.SysClassificationUrlService;
import org.springframework.web.bind.annotation.*;

/**
 * @author hugo tangxkwork@163.com
 * @description 分类URL 控制器
 * @version 1.0.0
 * @date 2024-10-11
 */
@RestController
@RequestMapping("/api/user/v1/classification-urls")
@Tag(name = "分类URL")
public class SysClassificationUrlController {
    @Resource
    private SysClassificationUrlService sysClassificationUrlService;

    @PostMapping(value = "/add")
    @Operation(summary = "添加分类URL")
    public Result<String> save(@RequestBody @Valid SysClassificationUrlEntity entity) {
        sysClassificationUrlService.save(entity);
        return Result.success();
    }

    @PostMapping(value = "/delete-by-id")
    @Operation(summary = "根据ID删除分类URL")
    public Result<String> delete(@RequestBody @Valid SysClassificationUrlEntity entity) {
        sysClassificationUrlService.removeById(entity.getId());
        return Result.success();
    }
}

