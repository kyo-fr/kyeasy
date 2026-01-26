package org.ares.cloud.merchantInfo.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.UserAgentUtils;
import org.ares.cloud.merchantInfo.dto.MerchantSocializeDto;
import org.springdoc.core.annotations.ParameterObject;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.annotation.Resource;

import org.ares.cloud.merchantInfo.convert.MerchantSideBarConvert;
import org.ares.cloud.merchantInfo.entity.MerchantSideBarEntity;
import org.ares.cloud.merchantInfo.service.MerchantSideBarService;
import org.ares.cloud.merchantInfo.query.MerchantSideBarQuery;
import org.ares.cloud.merchantInfo.dto.MerchantSideBarDto;

/**
* @author hugo tangxkwork@163.com
* @description 商户侧栏 控制器
* @version 1.0.0
* @date 2024-10-09
*/
@RestController
@RequestMapping("/api/merchant/v1/sidebar")
@Tag(name="商户侧栏")
@AllArgsConstructor
@Slf4j
public class MerchantSideBarController {
    @Resource
    private MerchantSideBarService merchantSideBarService;


    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:page')")
    public Result<PageResult<MerchantSideBarDto>> page(@ParameterObject @Valid MerchantSideBarQuery query,HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        log.info("page...domainName:{}",domainName);
        PageResult<MerchantSideBarDto> page = merchantSideBarService.loadList(query,domainName);
        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    @RequireUrlPermission
    // @PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:all')")
    public  Result<List<MerchantSideBarDto>> all(){
        List<MerchantSideBarDto> all = merchantSideBarService.loadAll();
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
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:info')")
    public Result<MerchantSideBarDto> get(@PathVariable("id") String id){
         MerchantSideBarDto dto= merchantSideBarService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "侧栏批量保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:save')")
    public Result<String> save(@RequestBody List<MerchantSideBarDto> dto){
        merchantSideBarService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "侧栏批量修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:update')")
    public Result<String> update(@RequestBody @Valid List<MerchantSideBarDto> dto){
        merchantSideBarService.update(dto);
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
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSideBar:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantSideBarService.deleteById(id);
        return Result.success();
    }


//    @GetMapping("/getMerchantSideBarByDomainName")
//    @Operation(summary = "根据域名查询商户社交详情")
//    public Result<List<MerchantSideBarDto>> getMerchantSideBarByDomainName(HttpServletRequest request){
//        String domainName = UserAgentUtils.getDomainOrIpPort(request);
//        log.info("getMerchantSideBarByDomainName...domainName:{}",domainName);
//        //根据域名查询对应商户信息
//        List<MerchantSideBarDto> merchantSideBarByDomainName = merchantSideBarService.getMerchantSideBarByDomainName(domainName);
//        return Result.success(merchantSideBarByDomainName);
//    }

//    @Parameter(
//            name = "tenantId",
//            description = "商户id",
//            required = true,
//            in = ParameterIn.PATH,
//            schema = @Schema(type = "string")
//    )
//    @GetMapping("{tenantId}")
//    @Operation(summary = "根据商户id查询侧栏")
//    public Result<List<MerchantSideBarDto>> get(@PathVariable("tenantId") String tenantId){
//        merchantSideBarService.loadByTenantId(tenantId);
//        return Result.success(dto);
//    }



}