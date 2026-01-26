package com.ares.cloud.order.interfaces.rest;

import com.ares.cloud.order.application.command.*;
import com.ares.cloud.order.domain.model.aggregate.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.api.order.commod.CreateOrderCommand;
import com.ares.cloud.order.application.service.OrderApplicationService;
import org.springframework.web.bind.annotation.*;

/**
 * 订单操作接口
 */
@Tag(name = "订单操作接口")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderApplicationService orderApplicationService;

    // ========== 通用订单接口 ==========
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<String> createOrder(@RequestBody CreateOrderCommand command) {
        command.setSourceType(Order.SOURCE_TYPE_MERCHANT);
        String orderId = orderApplicationService.createOrder(command);
        return Result.success(orderId);
    }
    @Operation(summary = "创建线上订单")
    @PostMapping("online")
    public Result<String> createOnlineOrder(@RequestBody CreateOrderCommand command) {
        command.setSourceType(Order.SOURCE_TYPE_ONLINE);
        String orderId = orderApplicationService.createOrder(command);
        return Result.success(orderId);
    }
    @Operation(summary = "取消订单")
    @PutMapping("/cancel")
    public Result<String> cancelOrder(@RequestBody CancelOrderCommand command) {
        orderApplicationService.cancelOrder(command);
        return Result.success();
    }

    @Operation(summary = "确认订单")
    @PutMapping("/{orderId}/confirm")
    public Result<String> confirmOrder(@PathVariable(name="orderId") String orderId) {
        orderApplicationService.confirmOrder(orderId);
        return Result.success();
    }

    @Operation(summary = "完成订单")
    @PutMapping("/{orderId}/complete")
    public Result<String> completeOrder(@PathVariable(name="orderId") String orderId) {
        orderApplicationService.completeOrder(orderId);
        return Result.success();
    }

    @Operation(summary = "删除订单项")
    @PutMapping("/delete/items")
    public Result<String> deleteOrderItem(@RequestBody DeleteOrderItemCommand command) {
        orderApplicationService.deleteOrderItem(command);
        return Result.success();
    }
    // ========== 配送订单接口 ==========
    @Operation(
            summary = "骑手接单",
            description = "商户自配送时，骑手接单操作。接单后订单状态变更为已接单，等待骑手开始配送。"
    )
    @PutMapping("/delivery/accept")
    @RequireUrlPermission
    public Result<String> acceptDelivery(@RequestBody AcceptDeliveryCommand command) {
        orderApplicationService.acceptDelivery(command);
        return Result.success();
    }

    @Operation(
            summary = "开始商户自配送",
            description = "商户自配送时，骑手接单后开始配送操作。开始配送后订单状态变更为配送中。"
    )
    @PutMapping("/{orderId}/delivery/start/merchant")
    @RequireUrlPermission
    public Result<String> startMerchantDelivery(@PathVariable String orderId) {
        orderApplicationService.startMerchantDelivery(orderId);
        return Result.success();
    }

    @Operation(
            summary = "开始三方配送",
            description = "三方配送时，直接开始配送操作。开始配送后订单状态变更为配送中。"
    )
    @PutMapping("/delivery/start/third-party")
    @RequireUrlPermission
    public Result<String> startThirdPartyDelivery(@RequestBody StartThirdPartyDeliveryCommand command) {
        orderApplicationService.startThirdPartyDelivery(command);
        return Result.success();
    }


    @Operation(summary = "完成配送")
    @PutMapping("/{orderId}/delivery/complete")
    @RequireUrlPermission
    public Result<String> completeDelivery(@PathVariable(name="orderId") String orderId) {
        orderApplicationService.completeDelivery(orderId);
        return Result.success();
    }

    // ========== 店内就餐订单接口 ==========
    @Operation(summary = "开始就餐")
    @PutMapping("/{orderId}/dining/start")
    public Result<String> startDining(@PathVariable(name="orderId") String orderId) {
        orderApplicationService.startDining(orderId);
        return Result.success();
    }

    // ========== 自取订单接口 ==========
//    @Operation(summary = "准备取餐")
//    @PutMapping("/{orderId}/pickup/ready")
//    public Result<String> readyForPickup(@PathVariable(name="orderId") String orderId) {
//        orderApplicationService.readyForPickup(orderId);
//        return Result.success();
//    }

    // ========== 预订订单接口 ==========
    @Operation(
            summary = "修改预订信息",
            description = "修改预订订单的预订信息，包括预订时间、预订人信息等"
    )
    @PutMapping("/reservation")
    public Result<String> updateReservationInfo(@RequestBody UpdateReservationCommand command) {
        orderApplicationService.updateReservationInfo(command);
        return Result.success();
    }
}