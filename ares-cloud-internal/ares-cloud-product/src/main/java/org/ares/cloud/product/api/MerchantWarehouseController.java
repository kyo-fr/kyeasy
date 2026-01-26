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
import org.ares.cloud.product.dto.MerchantWarehouseDto;
import org.ares.cloud.product.query.MerchantWarehouseQuery;
import org.ares.cloud.product.service.MerchantWarehouseService;
import org.ares.cloud.product.vo.MerchantWarehouseVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商户仓库主数据 控制器
* @version 1.0.0
* @date 2025-03-22
*/
@RestController
@RequestMapping("/api/merchant/v1/warehouse")
@Tag(name="商户仓库主数据")
@AllArgsConstructor
public class MerchantWarehouseController {
    @Resource
    private MerchantWarehouseService merchantWarehouseService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchantWarehouse:page')")
    public Result<PageResult<MerchantWarehouseVo>> page(@ParameterObject @Valid MerchantWarehouseQuery query){
        PageResult<MerchantWarehouseVo> page = merchantWarehouseService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:merchantWarehouse:all')")
    public  Result<List<MerchantWarehouseDto>> all(){
        List<MerchantWarehouseDto> all = merchantWarehouseService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:merchantWarehouse:info')")
    public Result<MerchantWarehouseDto> get(@PathVariable("id") String id){
         MerchantWarehouseDto dto= merchantWarehouseService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchantWarehouse:save')")
    public Result<String> save(@RequestBody @Valid MerchantWarehouseDto dto){
        merchantWarehouseService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:merchantWarehouse:update')")
    public Result<String> update(@RequestBody @Valid MerchantWarehouseDto dto){
        merchantWarehouseService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:merchantWarehouse:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantWarehouseService.deleteById(id);
        return Result.success();
    }



    @PostMapping("/upsert")
    @Operation(summary = "保存/更新")
    @RequireUrlPermission
    public Result<String> upsert(@RequestBody @Valid MerchantWarehouseDto dto){
        merchantWarehouseService.upsert(dto);
        return Result.success();
    }

}