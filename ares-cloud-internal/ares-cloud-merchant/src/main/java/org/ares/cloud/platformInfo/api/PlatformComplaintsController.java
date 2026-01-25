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
import org.ares.cloud.platformInfo.dto.PlatformComplaintsDto;
import org.ares.cloud.platformInfo.query.PlatformComplaintsQuery;
import org.ares.cloud.platformInfo.service.PlatformComplaintsService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 平台投诉建议 控制器
* @version 1.0.0
* @date 2024-10-17
*/
@RestController
@RequestMapping("/api/platform/v1/complaints")
@Tag(name="平台投诉建议")
@AllArgsConstructor
public class PlatformComplaintsController {
    @Resource
    private PlatformComplaintsService platformComplaintsService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:page')")
    public Result<PageResult<PlatformComplaintsDto>> page(@ParameterObject @Valid PlatformComplaintsQuery query){
        PageResult<PlatformComplaintsDto> page = platformComplaintsService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:all')")
    public  Result<List<PlatformComplaintsDto>> all(){
        List<PlatformComplaintsDto> all = platformComplaintsService.loadAll();
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:info')")
    public Result<PlatformComplaintsDto> get(@PathVariable("id") String id){
         PlatformComplaintsDto dto= platformComplaintsService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:save')")
    public Result<String> save(@RequestBody @Valid PlatformComplaintsDto dto){
        platformComplaintsService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:update')")
    public Result<String> update(@RequestBody @Valid PlatformComplaintsDto dto){
        platformComplaintsService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformComplaints:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformComplaintsService.deleteById(id);
        return Result.success();
    }


}