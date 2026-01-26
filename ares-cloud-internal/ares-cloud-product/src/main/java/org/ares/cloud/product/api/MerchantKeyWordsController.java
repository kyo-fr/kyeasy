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
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.product.dto.MerchantKeyWordsDto;
import org.ares.cloud.product.query.MerchantKeyWordsQuery;
import org.ares.cloud.product.service.MerchantKeyWordsService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商户关键字 控制器
* @version 1.0.0
* @date 2024-10-11
*/
@RestController
@RequestMapping("/api/merchant/v1/keywords")
@Tag(name="商户关键字")
@AllArgsConstructor
public class MerchantKeyWordsController {
    @Resource
    private MerchantKeyWordsService merchantKeyWordsService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:MerchantKeyWords:page')")
    public Result<PageResult<MerchantKeyWordsDto>> page(@ParameterObject @Valid MerchantKeyWordsQuery query){
        PageResult<MerchantKeyWordsDto> page = merchantKeyWordsService.loadList(query);
        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('product:MerchantKeyWords:all')")
    public  Result<List<MerchantKeyWordsDto>> all(){
        List<MerchantKeyWordsDto> all = merchantKeyWordsService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:MerchantKeyWords:info')")
    public Result<MerchantKeyWordsDto> get(@PathVariable("id") String id){
         MerchantKeyWordsDto dto= merchantKeyWordsService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:MerchantKeyWords:save')")
    public Result<String> save(@RequestBody @Valid MerchantKeyWordsDto dto) {
        try {
            merchantKeyWordsService.create(dto);
            return Result.success();
        } catch (Exception e) {
            throw new RpcCallException(e);
        }
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:MerchantKeyWords:update')")
    public Result<String> update(@RequestBody @Valid MerchantKeyWordsDto dto){
         merchantKeyWordsService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:MerchantKeyWords:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantKeyWordsService.deleteById(id);
        return Result.success();
    }


}