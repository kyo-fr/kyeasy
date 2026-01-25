package com.ares.cloud.pay.interfaces.rest;

import com.ares.cloud.pay.application.command.*;
import com.ares.cloud.pay.application.dto.MerchantDetailDTO;
import com.ares.cloud.pay.application.dto.MerchantSummaryDTO;
import com.ares.cloud.pay.application.dto.MerchantStatistics;
import com.ares.cloud.pay.application.dto.AccountFlowDTO;
import com.ares.cloud.pay.application.handlers.MerchantCommandHandler;
import com.ares.cloud.pay.application.handlers.MerchantQueryHandler;
import com.ares.cloud.pay.application.handlers.AccountFlowQueryHandler;
import com.ares.cloud.pay.application.queries.EntityQuery;
import com.ares.cloud.pay.application.queries.AccountFlowQuery;
import com.ares.cloud.pay.application.queries.MerchantStatisticsQuery;
import com.ares.cloud.pay.application.queries.PlatformFlowQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 商户管理控制器
 */
@RestController
@RequestMapping("/api/merchants")
@Tag(name = "商户管理", description = "商户相关接口")
public class MerchantController {

    @Resource
    private MerchantCommandHandler merchantCommandHandler;

    @Resource
    private MerchantQueryHandler merchantQueryHandler;
    
    @Resource
    private AccountFlowQueryHandler accountFlowQueryHandler;

    // ==================== 命令操作 ====================

    @PostMapping("/open")
    @Operation(summary = "开通商户", description = "通过商户ID、支持的支付区域和支付密码开通商户，需要提供确认密码。密码要求：至少6位字符，包含数字和字母，不能包含特殊字符")
    public Result<String> openMerchant(@RequestBody OpenMerchantCommand command) {
        String merchantIdResult = merchantCommandHandler.handleOpenMerchant(command);
        return Result.success(merchantIdResult);
    }

    @PutMapping("/{merchantId}/status")
    @Operation(summary = "更新商户状态", description = "更新商户的状态（ACTIVE/FROZEN/CLOSED）")
    public Result<String> updateMerchantStatus(@PathVariable String merchantId, @RequestBody UpdateMerchantStatusCommand command) {
        command.setMerchantId(merchantId);
        merchantCommandHandler.handleUpdateMerchantStatus(command);
        return Result.success("状态更新成功");
    }

    @PutMapping("/{merchantId}/password")
    @Operation(summary = "更新商户密码", description = "更新商户的支付密码，需要提供旧密码和新密码")
    public Result<String> updateMerchantPassword(@PathVariable String merchantId, @RequestBody UpdatePasswordCommand command) {
        command.setEntityType(UpdatePasswordCommand.EntityType.MERCHANT);
        command.setEntityId(merchantId);
        merchantCommandHandler.handleUpdatePassword(command);
        return Result.success("密码更新成功");
    }

    @PutMapping("/{merchantId}/regions")
    @Operation(summary = "更新商户支持的支付区域", description = "更新商户支持的支付区域列表")
    public Result<String> updateMerchantRegions(@PathVariable String merchantId, @RequestBody UpdateMerchantRegionsCommand command) {
        command.setMerchantId(merchantId);
        merchantCommandHandler.handleUpdateMerchantRegions(command);
        return Result.success("支付区域更新成功");
    }

    @PostMapping("/{merchantId}/regenerate-key")
    @Operation(summary = "重新生成商户密钥", description = "重新生成商户的支付密钥，用于API调用签名")
    public Result<String> regenerateMerchantKey(@PathVariable String merchantId) {
        RegenerateMerchantKeyCommand command = new RegenerateMerchantKeyCommand();
        command.setMerchantId(merchantId);
        merchantCommandHandler.handleRegenerateMerchantKey(command);
        return Result.success("密钥重新生成成功");
    }

    // ==================== 查询操作 ====================

    @GetMapping("/{merchantId}")
    @Operation(summary = "根据商户ID查询商户详情", description = "返回商户的详细信息，包括五个区域的钱包余额信息")
    public Result<MerchantDetailDTO> getMerchantById(@PathVariable String merchantId) {
        MerchantDetailDTO merchant = merchantQueryHandler.getMerchantById(merchantId);
        return Result.success(merchant);
    }

    @GetMapping("/info")
    @Operation(summary = "查询商户详情", description = "返回当前用户的商户详细信息，包括五个区域的钱包余额信息")
    public Result<MerchantDetailDTO> getMerchantByUserId() {
        String userId = ApplicationContext.getTenantId();
        MerchantDetailDTO merchant = merchantQueryHandler.getMerchantByUserId(userId);
        return Result.success(merchant);
    }

    @GetMapping("/by-merchant-no/{merchantNo}")
    @Operation(summary = "根据商户号查询商户详情", description = "返回商户的详细信息，包括五个区域的钱包余额信息")
    public Result<MerchantDetailDTO> getMerchantByMerchantNo(@PathVariable String merchantNo) {
        MerchantDetailDTO merchant = merchantQueryHandler.getMerchantByMerchantNo(merchantNo);
        return Result.success(merchant);
    }

    @GetMapping("/by-phone")
    @Operation(summary = "根据国家编号和手机号查询商户详情", description = "返回商户的详细信息，包括五个区域的钱包余额信息")
    public Result<MerchantDetailDTO> getMerchantByCountryCodeAndPhone(
            @RequestParam String countryCode, 
            @RequestParam String phone) {
        MerchantDetailDTO merchant = merchantQueryHandler.getMerchantByCountryCodeAndPhone(countryCode, phone);
        return Result.success(merchant);
    }

    @GetMapping
    @Operation(summary = "查询商户列表", description = "返回商户的摘要信息列表，不包含钱包余额信息")
    public Result<PageResult<MerchantSummaryDTO>> getMerchantList(@ParameterObject @Valid EntityQuery query) {
        PageResult<MerchantSummaryDTO> result = merchantQueryHandler.getMerchantList(query);
        return Result.success(result);
    }
    
    // ==================== 交易流水查询 ====================
    
    @GetMapping("/flows")
    @Operation(summary = "查询商户交易流水", description = "查询当前商户的交易流水记录，包含交易对方的国家编号和手机号信息")
    public Result<PageResult<AccountFlowDTO>> getMerchantAccountFlows(@ParameterObject @Valid AccountFlowQuery query) {
        PageResult<AccountFlowDTO> result = accountFlowQueryHandler.getMerchantAccountFlows(query);
        return Result.success(result);
    }
    
    @GetMapping("/{merchantId}/flows")
    @Operation(summary = "根据商户ID查询交易流水", description = "查询指定商户的交易流水记录，包含交易对方的国家编号和手机号信息")
    public Result<PageResult<AccountFlowDTO>> getMerchantFlowsByMerchantId(@PathVariable String merchantId,@ParameterObject @Valid AccountFlowQuery query) {
        PageResult<AccountFlowDTO> result = accountFlowQueryHandler.getAccountFlowsByAccountId(merchantId, query);
        return Result.success(result);
    }
    @GetMapping("/platform/flows")
    @Operation(summary = "查询平台交易流水", description = "查询平台的交易流水")
    @RequireUrlPermission
    public Result<PageResult<AccountFlowDTO>> getPlatformFlows(@ParameterObject @Valid PlatformFlowQuery query) {
        PageResult<AccountFlowDTO> result = accountFlowQueryHandler.getPlatformFlows(query);
        return Result.success(result);
    }
    
    // ==================== 商户统计查询 ====================
    
    @GetMapping("/statistics")
    @Operation(summary = "查询商户统计信息", description = "查询当前商户的统计信息，包括手续费、税费、各类收支明细等。支持按天/月/年查询，可按支付区域筛选")
    public Result<MerchantStatistics> getMerchantStatistics(@ParameterObject @Valid MerchantStatisticsQuery query) {
        MerchantStatistics result = merchantQueryHandler.getMerchantStatistics(query);
        return Result.success(result);
    }
} 