package org.ares.cloud.merchantInfo.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.exception.RequestBadException;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.StringUtils;
import org.ares.cloud.common.utils.UserAgentUtils;
import org.ares.cloud.merchantInfo.dto.MerchantInfoDto;
import org.ares.cloud.merchantInfo.enums.MerchantError;
import org.ares.cloud.merchantInfo.query.MerchantInfoQuery;
import org.ares.cloud.merchantInfo.service.MerchantInfoService;
import org.ares.cloud.merchantInfo.vo.MerchantInfoVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 商户信息 控制器
* @version 1.0.0
* @date 2024-10-08
*/
@RestController
@Tag(name="商户信息")
@AllArgsConstructor
@RequestMapping("/api/merchant/v1/infos")
public class MerchantInfoController {
    @Resource
    private MerchantInfoService merchantInfoService;


    @GetMapping("page")
    @Operation(summary = "分页" ,hidden = true)
    public Result<PageResult<MerchantInfoDto>> page(@ParameterObject @Valid MerchantInfoQuery query){
        PageResult<MerchantInfoDto> page = merchantInfoService.loadList(query);
        return Result.success(page);
    }


    @Operation(summary = "获取所有",hidden = true)
    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('merchantInfo:商户:all')")
    public  Result<List<MerchantInfoDto>> all(){
        List<MerchantInfoDto> all = merchantInfoService.loadAll();
        return Result.success(all);
    }

    @Parameters(
            { @Parameter(
                    name = "countryCode",
                    description = "国家代码",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "string")
            ), @Parameter( name = "registerPhone",
                    description = "注册手机号",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "string"))}
    )
    @GetMapping("{countryCode}/{registerPhone}")
    @Operation(summary = "根据手机号获取商户详情")
    public Result<MerchantInfoVo> getMerchantInfoByMobile(@PathVariable("countryCode") String countryCode,@PathVariable("registerPhone") String registerPhone){
        MerchantInfoVo dto= merchantInfoService.getMerchantInfoByMobile(countryCode,registerPhone);
         return Result.success(dto);
    }


    @GetMapping("")
    @Operation(summary = "获取商户详情(根据请求头域名查询)")
    public Result<MerchantInfoVo> getMerchantInfoByDomainName(HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        MerchantInfoVo dto= merchantInfoService.getMerchantInfoByDomainName(domainName);
        return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "商户信息更新")
    @RequireUrlPermission
    public Result<MerchantInfoDto> save(@RequestBody @Valid MerchantInfoDto dto){
        return Result.success(merchantInfoService.create(dto));
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('merchantInfo:商户:update')")
    public Result<String> update(@RequestBody @Valid MerchantInfoDto dto){
        merchantInfoService.update(dto);
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
    @Operation(summary = "根据id删除",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('merchantInfo:商户:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantInfoService.deleteById(id);
        return Result.success();
    }



    @Parameters(
            { @Parameter(
                    name = "id",
                    description = "商户id",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "string")
            )}
    )
    @GetMapping("{id}")
    @Operation(summary = "根据商户id获取详情")
    public Result<MerchantInfoDto> getMerchantInfoById(@PathVariable("id") String id){
        return Result.success(merchantInfoService.getMerchantInfoById(id));
    }


    @GetMapping("/getMerchantInfoByHeardId")
    @Operation(summary = "获取商户详情(根据请求头商户id查询)")
    public Result<MerchantInfoDto> getMerchantInfoByHeardId(){
        String tenantId = ApplicationContext.getTenantId();
        if (StringUtils.isBlank(tenantId)){
            throw new RequestBadException(MerchantError.MERCHANT_INFO_IS_NOT_EXIST_ERROR);
        }
        return Result.success( merchantInfoService.getMerchantInfoById(tenantId));
    }

}