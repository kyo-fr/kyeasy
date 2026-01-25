package org.ares.cloud.platformInfo.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.springdoc.core.annotations.ParameterObject;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.ares.cloud.platformInfo.convert.PlatformSubscribeConvert;
import org.ares.cloud.platformInfo.entity.PlatformSubscribeEntity;
import org.ares.cloud.platformInfo.service.PlatformSubscribeService;
import org.ares.cloud.platformInfo.query.PlatformSubscribeQuery;
import org.ares.cloud.platformInfo.dto.PlatformSubscribeDto;

/**
* @author hugo tangxkwork@163.com
* @description 订阅基础信息 控制器
* @version 1.0.0
* @date 2024-10-31
*/
@RestController
@RequestMapping("/api/platform/v1/subscribe")
@Tag(name="订阅基础信息")
@AllArgsConstructor
public class PlatformSubscribeController {
    @Resource
    private PlatformSubscribeService platformSubscribeService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
    public Result<PageResult<PlatformSubscribeDto>> page(@ParameterObject @Valid PlatformSubscribeQuery query){
        PageResult<PlatformSubscribeDto> page = platformSubscribeService.loadList(query);
        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    public  Result<List<PlatformSubscribeDto>> all(){
        List<PlatformSubscribeDto> all = platformSubscribeService.loadAll();
        return Result.success(all);
    }

    @Parameter(
        name = "id",
        description = "主键",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @GetMapping("{id}")
    @Operation(summary = "详情")
    @RequireUrlPermission
    public Result<PlatformSubscribeDto> get(@PathVariable("id") String id){
         PlatformSubscribeDto dto= platformSubscribeService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
    public Result<String> save(@RequestBody @Valid PlatformSubscribeDto dto){
        platformSubscribeService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformSubscribe:update')")
    public Result<String> update(@RequestBody @Valid PlatformSubscribeDto dto){
        platformSubscribeService.update(dto);
        return Result.success();
    }

    @Parameter(
    name = "id",
    description = "主键",
    required = true,
    in = ParameterIn.PATH,
    schema = @Schema(type = "string")
    )
    @DeleteMapping("{id}")
    @Operation(summary = "根据id删除" ,hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformSubscribe:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformSubscribeService.deleteById(id);
        return Result.success();
    }


}