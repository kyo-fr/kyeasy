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
import org.ares.cloud.product.dto.MerchantMarkingDto;
import org.ares.cloud.product.query.MerchantMarkingQuery;
import org.ares.cloud.product.service.MerchantMarkingService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 标注主数据 控制器
* @version 1.0.0
* @date 2025-03-19
*/
@RestController
@RequestMapping("/api/merchant/v1/marking")
@Tag(name="标注主数据")
@AllArgsConstructor
public class MerchantMarkingController {
    @Resource
    private MerchantMarkingService merchantMarkingService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchantMarking:page')")
    public Result<PageResult<MerchantMarkingDto>> page(@ParameterObject @Valid MerchantMarkingQuery query){
        PageResult<MerchantMarkingDto> page = merchantMarkingService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:merchantMarking:all')")
    public  Result<List<MerchantMarkingDto>> all(){
        List<MerchantMarkingDto> all = merchantMarkingService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:merchantMarking:info')")
    public Result<MerchantMarkingDto> get(@PathVariable("id") String id){
         MerchantMarkingDto dto= merchantMarkingService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:merchantMarking:save')")
    public Result<String> save(@RequestBody @Valid MerchantMarkingDto dto){
        merchantMarkingService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:merchantMarking:update')")
    public Result<String> update(@RequestBody @Valid MerchantMarkingDto dto){
        merchantMarkingService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:merchantMarking:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantMarkingService.deleteById(id);
        return Result.success();
    }


}