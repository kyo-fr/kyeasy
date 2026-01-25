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

import org.ares.cloud.platformInfo.convert.PlatformLanguageConvert;
import org.ares.cloud.platformInfo.entity.PlatformLanguageEntity;
import org.ares.cloud.platformInfo.service.PlatformLanguageService;
import org.ares.cloud.platformInfo.query.PlatformLanguageQuery;
import org.ares.cloud.platformInfo.dto.PlatformLanguageDto;

/**
* @author hugo tangxkwork@163.com
* @description 平台设置语言 控制器
* @version 1.0.0
* @date 2024-10-15
*/
@RestController
@RequestMapping("/api/platform/v1/language")
@Tag(name="平台设置语言")
@AllArgsConstructor
public class PlatformLanguageController {
    @Resource
    private PlatformLanguageService platformLanguageService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:page')")
    public Result<PageResult<PlatformLanguageDto>> page(@ParameterObject @Valid PlatformLanguageQuery query){
        PageResult<PlatformLanguageDto> page = platformLanguageService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:all')")
    public  Result<List<PlatformLanguageDto>> all(){
        List<PlatformLanguageDto> all = platformLanguageService.loadAll();
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:info')")
    public Result<PlatformLanguageDto> get(@PathVariable("id") String id){
         PlatformLanguageDto dto= platformLanguageService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:save')")
    public Result<String> save(@RequestBody @Valid PlatformLanguageDto dto){
        platformLanguageService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:update')")
    public Result<String> update(@RequestBody @Valid PlatformLanguageDto dto){
        platformLanguageService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformLanguage:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformLanguageService.deleteById(id);
        return Result.success();
    }


}