package com.ares.cloud.payment.interfaces.web;

import com.ares.cloud.payment.application.dto.InvoiceDTO;
import com.ares.cloud.payment.application.dto.MerchantInvoiceDTO;
import com.ares.cloud.payment.application.dto.UserInvoiceDTO;
import com.ares.cloud.payment.application.query.InvoiceQuery;
import com.ares.cloud.payment.application.service.InvoiceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发票查询控制器
 * @author hugo tangxkwork@163.com
 * @version 1.0.0
 * @date 2024-10-23
 */
@RestController
@RequestMapping("/api/base/invoices")
@Tag(name = "发票查询")
@AllArgsConstructor
public class InvoiceQueryController {

    private final InvoiceQueryService invoiceQueryService;

    /**
     * 获取发票详情
     */
    @Parameter(
        name = "id",
        description = "发票ID",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @GetMapping("{id}")
    @Operation(summary = "详情查询")
    public Result<InvoiceDTO> get(@PathVariable("id") String id) {
        InvoiceDTO dto = invoiceQueryService.getInvoiceDetails(id);
        return Result.success(dto);
    }

    /**
     * 根据订单ID查询发票列表
     */
    @Parameter(
        name = "orderId",
        description = "订单ID",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @GetMapping("order/{orderId}")
    @Operation(summary = "根据订单ID查询发票列表")
    public Result<List<InvoiceDTO>> getByOrderId(@PathVariable("orderId") String orderId) {
        List<InvoiceDTO> list = invoiceQueryService.getInvoicesByOrderId(orderId);
        return Result.success(list);
    }

    /**
     * 商户查询发票列表
     */
    @GetMapping("/merchant/query")
    @Operation(summary = "商户查询发票列表")
    @RequireUrlPermission
    public Result<PageResult<MerchantInvoiceDTO>> merchantQuery(@ParameterObject @Valid InvoiceQuery query) {
        PageResult<MerchantInvoiceDTO> page = invoiceQueryService.getMerchantInvoices(query);
        return Result.success(page);
    }

    /**
     * 用户查询发票列表
     */
    @GetMapping("/user/query")
    @Operation(summary = "用户查询发票列表")
    public Result<PageResult<UserInvoiceDTO>> userQuery(@ParameterObject @Valid InvoiceQuery query) {
        PageResult<UserInvoiceDTO> page = invoiceQueryService.getUserInvoices(query);
        return Result.success(page);
    }
}