package org.ares.cloud.product.api;


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
import org.ares.cloud.product.dto.MerchantWarehouseSeatDto;
import org.ares.cloud.product.query.MerchantWarehouseSeatQuery;
import org.ares.cloud.product.service.MerchantWarehouseSeatService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商户仓库位子主数据 控制器
* @version 1.0.0
* @date 2025-03-22
*/
@RestController
@RequestMapping("/api/merchant/v1/merchantWarehouseSeat")
@Tag(name="商户仓库位子主数据")
@AllArgsConstructor
public class MerchantWarehouseSeatController {
    @Resource
    private MerchantWarehouseSeatService merchantWarehouseSeatService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchant_warehouse_seat:page')")
    public Result<PageResult<MerchantWarehouseSeatDto>> page(@ParameterObject @Valid MerchantWarehouseSeatQuery query){
        PageResult<MerchantWarehouseSeatDto> page = merchantWarehouseSeatService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:merchant_warehouse_seat:all')")
    public  Result<List<MerchantWarehouseSeatDto>> all(){
        List<MerchantWarehouseSeatDto> all = merchantWarehouseSeatService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:merchant_warehouse_seat:info')")
    public Result<MerchantWarehouseSeatDto> get(@PathVariable("id") String id){
        MerchantWarehouseSeatDto dto= merchantWarehouseSeatService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchant_warehouse_seat:save')")
    public Result<String> save(@RequestBody @Valid MerchantWarehouseSeatDto dto){
        merchantWarehouseSeatService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:merchant_warehouse_seat:update')")
    public Result<String> update(@RequestBody @Valid MerchantWarehouseSeatDto dto){
        merchantWarehouseSeatService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:merchant_warehouse_seat:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantWarehouseSeatService.deleteById(id);
        return Result.success();
    }


    @PostMapping("/upsert")
    @Operation(summary = "保存/更新")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:merchant_warehouse_seat:save')")
    public Result<String> upsert(@RequestBody @Valid MerchantWarehouseSeatDto dto){
        merchantWarehouseSeatService.upsert(dto);
        return Result.success();
    }

}