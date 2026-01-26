package org.ares.cloud.product.api;


import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.UserAgentUtils;
import org.ares.cloud.product.dto.ProductTypeDto;
import org.ares.cloud.product.query.ProductTypeQuery;
import org.ares.cloud.product.service.ProductTypeService;
import org.ares.cloud.product.vo.ProductTypeVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商品分类管理 控制器
* @version 1.0.0
* @date 2024-10-28
*/
@RestController
@RequestMapping("/api/product/v1/productType")
@Tag(name="商品分类管理")
@AllArgsConstructor
@Slf4j
public class ProductTypeController {
    @Resource
    private ProductTypeService productTypeService;


    @Operation(summary = "根据父级查询子集")
    @GetMapping("/getSonByParentId")
    // @PreAuthorize("hasAuthority('product:ProductType:all')")
    public  Result<JSONObject> getSonByParentId(@RequestParam(value = "parentId", required = false)  String parentId, @RequestParam("levels")  int levels){
        JSONObject getSonByParentId = productTypeService.getSonByParentId(parentId,levels);
        return Result.success(getSonByParentId);
    }

    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('product:ProductType:page')")
    public Result<PageResult<ProductTypeVo>> page(@ParameterObject @Valid ProductTypeQuery query){
        PageResult<ProductTypeVo> page = productTypeService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('product:ProductType:all')")
    public  Result<List<ProductTypeDto>> all(){
        List<ProductTypeDto> all = productTypeService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:ProductType:info')")
    public Result<ProductTypeDto> get(@PathVariable("id") String id){
         ProductTypeDto dto= productTypeService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:ProductType:save')")
    public Result<String> save(@RequestBody @Valid ProductTypeDto dto){
        productTypeService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:ProductType:update')")
    public Result<String> update(@RequestBody @Valid ProductTypeDto dto){
        productTypeService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:ProductType:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        productTypeService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "根据租户查询所有分类")
    @GetMapping("/getTypeByTenantId")
    // @PreAuthorize("hasAuthority('product:ProductType:all')")
    public  Result< List<ProductTypeVo>> getTypeByTenantId(HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        log.info("getTypeByTenantId...domainName:{}",domainName);
        List<ProductTypeVo> typeByTenantId = productTypeService.getTypeByDomainName(domainName);
        return Result.success(typeByTenantId);
    }

//    @Operation(summary = "根据类型id获取商品信息")
//    @GetMapping("getProductInfoByTypeId")
//    public   Result<PageResult<ProductDto>> getProductInfoByTypeId(@RequestParam("typeId") String typeId,@RequestParam("levels") int levels){
//        PageResult<ProductDto> page = productTypeService.getProductInfoByTypeId(typeId,levels);
//        return Result.success(page);
//    }

}