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
import org.ares.cloud.product.dto.MerchantSpecificationDto;
import org.ares.cloud.product.query.MerchantSpecificationQuery;
import org.ares.cloud.product.service.MerchantSpecificationService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 主规格主数据 控制器
* @version 1.0.0
* @date 2025-03-18
*/
@RestController
@RequestMapping("/api/merchant/v1/specification")
@Tag(name="主规格主数据")
@AllArgsConstructor
public class MerchantSpecificationController {
    @Resource
    private MerchantSpecificationService merchantSpecificationService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:MerchantSpecification:page')")
    public Result<PageResult<MerchantSpecificationDto>> page(@ParameterObject @Valid MerchantSpecificationQuery query){
        PageResult<MerchantSpecificationDto> page = merchantSpecificationService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:MerchantSpecification:all')")
    public  Result<List<MerchantSpecificationDto>> all(){
        List<MerchantSpecificationDto> all = merchantSpecificationService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:MerchantSpecification:info')")
    public Result<MerchantSpecificationDto> get(@PathVariable("id") String id){
         MerchantSpecificationDto dto= merchantSpecificationService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:MerchantSpecification:save')")
    public Result<String> save(@RequestBody @Valid MerchantSpecificationDto dto){
        merchantSpecificationService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:MerchantSpecification:update')")
    public Result<String> update(@RequestBody @Valid MerchantSpecificationDto dto){
        merchantSpecificationService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:MerchantSpecification:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantSpecificationService.deleteById(id);
        return Result.success();
    }


}