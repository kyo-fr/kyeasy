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
import org.ares.cloud.platformInfo.dto.PlatformInfoDto;
import org.springdoc.core.annotations.ParameterObject;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.ares.cloud.platformInfo.convert.PlatformSocializeConvert;
import org.ares.cloud.platformInfo.entity.PlatformSocializeEntity;
import org.ares.cloud.platformInfo.service.PlatformSocializeService;
import org.ares.cloud.platformInfo.query.PlatformSocializeQuery;
import org.ares.cloud.platformInfo.dto.PlatformSocializeDto;

/**
* @author hugo tangxkwork@163.com
* @description 平台海外社交 控制器
* @version 1.0.0
* @date 2024-10-15
*/
@RestController
@RequestMapping("/api/platform/v1/socialize")
@Tag(name="平台海外社交")
@AllArgsConstructor
public class PlatformSocializeController {
    @Resource
    private PlatformSocializeService platformSocializeService;


    @GetMapping("page")
    @Operation(summary = "分页",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:page')")
    public Result<PageResult<PlatformSocializeDto>> page(@ParameterObject @Valid PlatformSocializeQuery query){
        PageResult<PlatformSocializeDto> page = platformSocializeService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有",hidden = true)
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:all')")
    public  Result<List<PlatformSocializeDto>> all(){
        List<PlatformSocializeDto> all = platformSocializeService.loadAll();
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:info')")
    public Result<PlatformSocializeDto> get(@PathVariable("id") String id){
         PlatformSocializeDto dto= platformSocializeService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存/更新(id必传)")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:save')")
    public Result<String> save(@RequestBody @Valid PlatformSocializeDto dto){
        platformSocializeService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:update')")
    public Result<String> update(@RequestBody @Valid PlatformSocializeDto dto){
        platformSocializeService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformSocialize:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformSocializeService.deleteById(id);
        return Result.success();
    }

    @GetMapping("")
    @Operation(summary = "获取平台社交信息")
    @RequireUrlPermission
    public Result<PlatformSocializeDto> getInfoByUserId(){
        PlatformSocializeDto dto= platformSocializeService.getInfoByUserId();
        return Result.success(dto);
    }

}