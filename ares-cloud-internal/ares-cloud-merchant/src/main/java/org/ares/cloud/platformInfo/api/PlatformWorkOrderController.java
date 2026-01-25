package org.ares.cloud.platformInfo.api;


import io.swagger.v3.oas.annotations.Hidden;
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
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.platformInfo.dto.PlatformWorkOrderDto;
import org.ares.cloud.platformInfo.query.PlatformWorkOrderQuery;
import org.ares.cloud.platformInfo.service.PlatformWorkOrderService;
import org.ares.cloud.platformInfo.vo.PlatformWorkOrderVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 工单 控制器
* @version 1.0.0
* @date 2024-10-16
*/
@RestController
@RequestMapping("/api/platform/v1/workOrder")
@Tag(name="工单-废弃")
@Hidden
@AllArgsConstructor
public class PlatformWorkOrderController {
    @Resource
    private PlatformWorkOrderService platformWorkOrderService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:page')")
    public Result<PageResult<PlatformWorkOrderVo>> page(@ParameterObject @Valid PlatformWorkOrderQuery query){
        PageResult<PlatformWorkOrderVo> page = platformWorkOrderService.loadList(query);
        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:all')")
    public  Result<List<PlatformWorkOrderDto>> all(){
        List<PlatformWorkOrderDto> all = platformWorkOrderService.loadAll();
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:info')")
    public Result<PlatformWorkOrderDto> get(@PathVariable("id") String id){
         PlatformWorkOrderDto dto= platformWorkOrderService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:save')")
    public Result<String> save(@RequestBody @Valid PlatformWorkOrderDto dto){
        try {
            platformWorkOrderService.create(dto);
            return Result.success();
        } catch (Exception e) {
            throw new RpcCallException(e);
        }
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:update')")
    public Result<String> update(@RequestBody @Valid PlatformWorkOrderDto dto){
        platformWorkOrderService.update(dto);
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
    //@PreAuthorize("hasAuthority('platformInfo:PlatformWorkOrder:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformWorkOrderService.deleteById(id);
        return Result.success();
    }


}