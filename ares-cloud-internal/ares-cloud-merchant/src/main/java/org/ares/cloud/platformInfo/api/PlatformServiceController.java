package org.ares.cloud.platformInfo.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.platformInfo.dto.PlatformServiceDto;
import org.ares.cloud.platformInfo.query.PlatformServiceQuery;
import org.ares.cloud.platformInfo.service.PlatformServiceService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 服务和帮助 控制器
* @version 1.0.0
* @date 2024-10-30
*/
@RestController
@RequestMapping("/api/platform/v1/serviceAndHelp")
@Tag(name="服务和帮助")
@AllArgsConstructor
public class PlatformServiceController {
    @Resource
    private PlatformServiceService platformServiceService;


    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('platformInfo:PlatformService:page')")
    public Result<PageResult<PlatformServiceDto>> page(@ParameterObject @Valid PlatformServiceQuery query){
        PageResult<PlatformServiceDto> page = platformServiceService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformService:all')")
    public  Result<List<PlatformServiceDto>> all(){
        List<PlatformServiceDto> all = platformServiceService.loadAll();
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformService:info')")
    public Result<PlatformServiceDto> get(@PathVariable("id") String id){
         PlatformServiceDto dto= platformServiceService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformService:save')")
    public Result<String> save(@RequestBody @Valid PlatformServiceDto dto){
        platformServiceService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformService:update')")
    public Result<String> update(@RequestBody @Valid PlatformServiceDto dto){
        platformServiceService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformService:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformServiceService.deleteById(id);
        return Result.success();
    }

}