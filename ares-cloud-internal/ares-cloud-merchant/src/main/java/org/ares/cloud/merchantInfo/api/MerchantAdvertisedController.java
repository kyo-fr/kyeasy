package org.ares.cloud.merchantInfo.api;


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
import org.ares.cloud.merchantInfo.dto.MerchantAdvertisedDto;
import org.ares.cloud.merchantInfo.dto.MerchantSocializeDto;
import org.ares.cloud.merchantInfo.query.MerchantAdvertisedQuery;
import org.ares.cloud.merchantInfo.service.MerchantAdvertisedService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
* @author hugo tangxkwork@163.com
* @description 商户广告 控制器
* @version 1.0.0
* @date 2025-01-03
*/
@RestController
@RequestMapping("/api/merchant/v1/merchantAdvertised")
@Tag(name="商户广告")
@AllArgsConstructor
@Slf4j
public class MerchantAdvertisedController {
    @Resource
    private MerchantAdvertisedService merchantAdvertisedService;


    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantAdvertised:page')")
    public Result<PageResult<MerchantAdvertisedDto>> page(@ParameterObject @Valid MerchantAdvertisedQuery query, HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        //根据域名查询对应商户信息
        PageResult<MerchantAdvertisedDto> page = merchantAdvertisedService.loadList(query,domainName);
        return Result.success(page);
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
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantAdvertised:info')")
    public Result<MerchantAdvertisedDto> get(@PathVariable("id") String id){
         MerchantAdvertisedDto dto= merchantAdvertisedService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantAdvertised:save')")
    public Result<String> save(@RequestBody @Valid MerchantAdvertisedDto dto){
        merchantAdvertisedService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantAdvertised:update')")
    public Result<String> update(@RequestBody @Valid MerchantAdvertisedDto dto){
        merchantAdvertisedService.update(dto);
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
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantAdvertised:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantAdvertisedService.deleteById(id);
        return Result.success();
    }


    @GetMapping("/getMerchantAdvertisedByDomainName")
    @Operation(summary = "根据域名查询商户广告")
    public Result<MerchantAdvertisedDto> getMerchantAdvertisedByDomainName(HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        log.info("getMerchantAdvertisedByDomainName...domainName:{}",domainName);
        //根据域名查询对应商户信息
        MerchantAdvertisedDto dto = merchantAdvertisedService.getMerchantAdvertisedByDomainName(domainName);
        return Result.success(dto);
    }

}