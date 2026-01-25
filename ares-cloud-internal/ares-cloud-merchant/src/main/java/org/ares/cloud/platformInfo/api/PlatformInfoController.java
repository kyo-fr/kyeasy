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

import org.ares.cloud.platformInfo.convert.PlatformInfoConvert;
import org.ares.cloud.platformInfo.entity.PlatformInfoEntity;
import org.ares.cloud.platformInfo.service.PlatformInfoService;
import org.ares.cloud.platformInfo.query.PlatformInfoQuery;
import org.ares.cloud.platformInfo.dto.PlatformInfoDto;

/**
* @author hugo tangxkwork@163.com
* @description 平台信息 控制器
* @version 1.0.0
* @date 2024-10-15
*/
@RestController
@RequestMapping("/api/platform/v1/infos")
@Tag(name="平台信息")
@AllArgsConstructor
public class PlatformInfoController {
    @Resource
    private PlatformInfoService platformInfoService;


    @GetMapping("page")
    @Operation(summary = "分页",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformInfo:page')")
    public Result<PageResult<PlatformInfoDto>> page(@ParameterObject @Valid PlatformInfoQuery query){
        PageResult<PlatformInfoDto> page = platformInfoService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有",hidden = true)
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformInfo:all')")
    public  Result<List<PlatformInfoDto>> all(){
        List<PlatformInfoDto> all = platformInfoService.loadAll();
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
    @Operation(summary = "详情",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformInfo:info')")
    public Result<PlatformInfoDto> get(@PathVariable("id") String id){
         PlatformInfoDto dto= platformInfoService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存/更新(传id)")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformInfo:save')")
    public Result<String> save(@RequestBody @Valid PlatformInfoDto dto){
        platformInfoService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformInfo:update')")
    public Result<String> update(@RequestBody @Valid PlatformInfoDto dto){
        platformInfoService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformInfo:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformInfoService.deleteById(id);
        return Result.success();
    }


    @GetMapping("")
    @Operation(summary = "获取平台信息")
    @RequireUrlPermission
    public Result<PlatformInfoDto> getInfoByUserId(){
        PlatformInfoDto dto= platformInfoService.getInfoByUserId();
        return Result.success(dto);
    }
}