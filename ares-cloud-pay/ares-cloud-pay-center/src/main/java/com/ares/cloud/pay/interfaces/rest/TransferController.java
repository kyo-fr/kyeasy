package com.ares.cloud.pay.interfaces.rest;

import com.ares.cloud.pay.application.command.*;
import org.ares.cloud.api.payment.dto.TransferResultDTO;
import com.ares.cloud.pay.application.handlers.TransferCommandHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.payment.command.GenericTransferCommand;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.model.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 转账控制器
 * 提供转账相关的REST接口
 */
@RestController
@RequestMapping("/api/v1/transfer")
@Tag(name = "转账管理", description = "转账相关接口")
public class TransferController {
    
    @Resource
    private TransferCommandHandler transferCommandHandler;
    
    /**
     * 用户间转账
     */
    @PostMapping("/user")
    @Operation(summary = "用户间转账", description = "支持用户之间的转账操作")
    public Result<TransferResultDTO> userTransfer(
            @Parameter(description = "转账请求参数", required = true)
            @Valid @RequestBody UserTransferCommand command) {
        TransferResultDTO result = transferCommandHandler.handleUserTransfer(command);
        return Result.success(result);
    }
    
    /**
     * 用户向商户付款
     */
    @PostMapping("/user-to-merchant")
    @Operation(summary = "用户向商户付款", description = "支持用户向商户付款")
    public Result<TransferResultDTO> userToMerchantPayment(
            @Parameter(description = "付款请求参数", required = true)
            @Valid @RequestBody UserToMerchantPaymentCommand command) {
        String userId = ApplicationContext.getUserId();
        TransferResultDTO result = transferCommandHandler.handleUserToMerchantPayment(userId, command);
        return Result.success(result);
    }
    
    /**
     * 商户向用户发放礼物点
     */
    @PostMapping("/merchant-to-user")
    @Operation(summary = "商户向用户发放礼物点", description = "商户向用户发放礼物点、奖励、佣金等")
    @RequireUrlPermission
    public Result<TransferResultDTO> merchantToUserPayment(
            @Parameter(description = "发放请求参数", required = true)
            @Valid @RequestBody MerchantToUserPaymentCommand command) {
        TransferResultDTO result = transferCommandHandler.handleMerchantToUserPayment(command);
        return Result.success(result);
    }
    
    /**
     * 商户充值
     */
    @PostMapping("/merchant/recharge")
    @Operation(summary = "商户充值", description = "从平台商户向普通商户充值")
    @RequireUrlPermission
    public Result<TransferResultDTO> merchantRecharge(
            @Parameter(description = "充值请求参数", required = true)
            @Valid @RequestBody MerchantRechargeCommand command) {
        TransferResultDTO result = transferCommandHandler.handleMerchantRecharge(command);
        return Result.success(result);
    }
    
    /**
     * 商户回收
     */
    @PostMapping("/merchant/deduction")
    @Operation(summary = "商户回收", description = "从普通商户向平台商户回收资金")
    @RequireUrlPermission
    public Result<TransferResultDTO> merchantDeduction(
            @Parameter(description = "回收请求参数", required = true)
            @Valid @RequestBody MerchantDeductionCommand command) {
        TransferResultDTO result = transferCommandHandler.handleMerchantDeduction(command);
        return Result.success(result);
    }
    
    /**
     * 通用转账
     */
    @PostMapping("/generic")
    @Operation(summary = "通用转账", description = "支持指定交易类型的灵活转账操作，可配置手续费承担方和密码验证")
    public Result<TransferResultDTO> genericTransfer(
            @Parameter(description = "通用转账请求参数", required = true)
            @Valid @RequestBody GenericTransferCommand command) {
        TransferResultDTO result = transferCommandHandler.handleGenericTransfer(command);
        return Result.success(result);
    }
} 