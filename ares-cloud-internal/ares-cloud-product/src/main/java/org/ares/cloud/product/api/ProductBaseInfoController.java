package org.ares.cloud.product.api;


import com.alibaba.fastjson.JSONObject;
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
import org.ares.cloud.product.dto.ProductBaseInfoDto;
import org.ares.cloud.product.dto.ProductDto;
import org.ares.cloud.product.query.ProductBaseInfoQuery;
import org.ares.cloud.product.service.ProductBaseInfoService;
import org.ares.cloud.product.vo.ProductBaseInfoVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商品信息 控制器
* @version 1.0.0
* @date 2024-11-06
*/
@RestController
@RequestMapping("api/product/v1/productBaseInfo")
@Tag(name="商品信息")
@AllArgsConstructor
@Slf4j
public class ProductBaseInfoController {
    @Resource
    private ProductBaseInfoService productBaseInfoService;


    @GetMapping("page")
    @Operation(summary = "分页")
    public Result<PageResult<ProductDto>> page(@ParameterObject @Valid ProductBaseInfoQuery query,HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        log.info("page...domainName:{}",domainName);
        //根据域名查询对应商户信息
        PageResult<ProductDto> page = productBaseInfoService.loadList(query, domainName);
        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('product:ProductBaseInfo:all')")
    public  Result<List<ProductBaseInfoDto>> all(){
        List<ProductBaseInfoDto> all = productBaseInfoService.loadAll();
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
    //@PreAuthorize("hasAuthority('product:ProductBaseInfo:info')")
    public Result<ProductBaseInfoVo> get(@PathVariable("id") String id){
        ProductBaseInfoVo dto= productBaseInfoService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('product:ProductBaseInfo:save')")
    public Result<String> save(@RequestBody @Valid ProductBaseInfoDto dto){
        productBaseInfoService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('product:ProductBaseInfo:update')")
    public Result<String> update(@RequestBody @Valid ProductBaseInfoDto dto){
        productBaseInfoService.update(dto);
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
    //@PreAuthorize("hasAuthority('product:ProductBaseInfo:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        productBaseInfoService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/updateProductByStatus")
    @Operation(summary = "商品上下架")
    @RequireUrlPermission
    public Result<String> updateProductByStatus(@RequestParam("isEnable")@Schema(description = "是否上下架 enable-上架;not_enable-下架") String isEnable,@RequestParam("productId") @Schema(description = "商品id") String productId){
        productBaseInfoService.updateProductByStatus(isEnable,productId);
        return Result.success();
    }



//    @Operation(summary = "根据类型id获取商品信息")
//    @GetMapping("getProductInfoByTypeId")
//    public   Result<PageResult<ProductBaseInfoDto>> getProductInfoByTypeId(@RequestParam("typeId") String typeId,@RequestParam("levels") int levels){
//        PageResult<ProductBaseInfoDto> page = productBaseInfoService.getProductInfoByTypeId(typeId,levels);
//        return Result.success(page);
//    }


    @Operation(summary = "根据类型id获取商品信息")
    @GetMapping("getProductInfoByTypeId")
    public Result<PageResult<ProductBaseInfoDto>> getProductInfoByTypeId(@ParameterObject @Valid ProductBaseInfoQuery query, HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        //根据域名查询对应商户信息
        PageResult<ProductBaseInfoDto> page = productBaseInfoService.getProductInfoByTypeId(query,domainName);
        return Result.success(page);
    }
}