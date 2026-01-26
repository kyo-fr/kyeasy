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
import org.ares.cloud.product.dto.MerchantSubSpecificationDto;
import org.ares.cloud.product.query.MerchantSubSpecificationQuery;
import org.ares.cloud.product.service.MerchantSubSpecificationService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 子规格主数据 控制器
* @version 1.0.0
* @date 2025-03-18
*/
@RestController
@RequestMapping("/api/merchant/v1/subSpecification")
@Tag(name="子规格主数据")
@AllArgsConstructor
public class MerchantSubSpecificationController {
    @Resource
    private MerchantSubSpecificationService merchantSubSpecificationService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:ProductSubSpecification:page')")
    public Result<PageResult<MerchantSubSpecificationDto>> page(@ParameterObject @Valid MerchantSubSpecificationQuery query){
        PageResult<MerchantSubSpecificationDto> page = merchantSubSpecificationService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:ProductSubSpecification:all')")
    public  Result<List<MerchantSubSpecificationDto>> all(){
        List<MerchantSubSpecificationDto> all = merchantSubSpecificationService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:ProductSubSpecification:info')")
    public Result<MerchantSubSpecificationDto> get(@PathVariable("id") String id){
         MerchantSubSpecificationDto dto= merchantSubSpecificationService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:ProductSubSpecification:save')")
    public Result<String> save(@RequestBody @Valid MerchantSubSpecificationDto dto){
        merchantSubSpecificationService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:ProductSubSpecification:update')")
    public Result<String> update(@RequestBody @Valid MerchantSubSpecificationDto dto){
        merchantSubSpecificationService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:ProductSubSpecification:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantSubSpecificationService.deleteById(id);
        return Result.success();
    }


}