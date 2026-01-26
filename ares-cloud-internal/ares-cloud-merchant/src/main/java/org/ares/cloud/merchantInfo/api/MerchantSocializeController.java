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
import org.springdoc.core.annotations.ParameterObject;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.annotation.Resource;

import org.ares.cloud.merchantInfo.convert.MerchantSocializeConvert;
import org.ares.cloud.merchantInfo.entity.MerchantSocializeEntity;
import org.ares.cloud.merchantInfo.service.MerchantSocializeService;
import org.ares.cloud.merchantInfo.query.MerchantSocializeQuery;
import org.ares.cloud.merchantInfo.dto.MerchantSocializeDto;

/**
* @author hugo tangxkwork@163.com
* @description 商户社交 控制器
* @version 1.0.0
* @date 2024-10-09
*/
@RestController
@RequestMapping("/api/merchant/v1/socialize")
@Tag(name="商户社交")
@AllArgsConstructor
@Slf4j
public class MerchantSocializeController {
    @Resource
    private MerchantSocializeService merchantSocializeService;


    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:page')")
    public Result<PageResult<MerchantSocializeDto>> page(@ParameterObject @Valid MerchantSocializeQuery query){
        PageResult<MerchantSocializeDto> page = merchantSocializeService.loadList(query);

        return Result.success(page);
    }


    @Operation(summary = "获取所有")
    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:all')")
    public  Result<List<MerchantSocializeDto>> all(){
        List<MerchantSocializeDto> all = merchantSocializeService.loadAll();
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
    @Operation(summary = "根据商户id查询商户社交详情")
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:info')")
    public Result<MerchantSocializeDto> get(@PathVariable("id") String id){
         MerchantSocializeDto dto= merchantSocializeService.loadById(id);
         return Result.success(dto);
    }


    @GetMapping("/getMerchantSocializeByDomainName")
    @Operation(summary = "根据域名查询商户社交详情")
    public Result<MerchantSocializeDto> getMerchantSocializeByDomainName(HttpServletRequest request){
        String domainName = UserAgentUtils.getDomainOrIpPort(request);
        log.info("getMerchantSocializeByDomainName...domainName:{}",domainName);
        //根据域名查询对应商户信息
        MerchantSocializeDto dto= merchantSocializeService.getMerchantSocializeByDomainName(domainName);
        return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "社交保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:save')")
    public Result<String> save(@RequestBody @Validated MerchantSocializeDto dto){
        merchantSocializeService.create(dto);
        return Result.success();
    }

//    @PutMapping
//    @Operation(summary = "修改")
//    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:update')")
//    public Result<String> update(@RequestBody @Valid MerchantSocializeDto dto){
//        merchantSocializeService.update(dto);
//        return Result.success();
//    }

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
    //@PreAuthorize("hasAuthority('merchantInfo:MerchantSocialize:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        merchantSocializeService.deleteById(id);
        return Result.success();
    }


}