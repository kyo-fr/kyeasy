package com.ares.cloud.payment.interfaces.web;

import com.ares.cloud.payment.application.command.MerchantInvoiceCommand;
import org.ares.cloud.api.payment.command.VoidInvoiceCommand;
import com.ares.cloud.payment.application.service.InvoiceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.model.Result;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 发票命令控制器
 * @author hugo tangxkwork@163.com
 * @version 1.0.0
 * @date 2024-10-23
 */
@RestController
@RequestMapping("/api/base/invoices")
@Tag(name = "发票操作")
@AllArgsConstructor
public class InvoiceCommandController {

    private final InvoiceCommandService invoiceCommandService;

    /**
     * 商户开票
     */
    @PostMapping("/merchant")
    @Operation(summary = "商户开票")
    @RequireUrlPermission
    public Result<String> merchantInvoice(@RequestBody @Valid MerchantInvoiceCommand command) {
        String invoiceId = invoiceCommandService.generateMerchantInvoice(command);
        return Result.success(invoiceId);
    }

    /**
     * 作废发票
     */
    @Parameter(
        name = "id",
        description = "发票ID",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @PostMapping("void")
    @Operation(summary = "作废发票")
    @RequireUrlPermission
    public Result<String> voidInvoice(@RequestBody VoidInvoiceCommand command) {
        boolean result = invoiceCommandService.voidInvoice(command);
        return result ? Result.success() : Result.error("作废失败");
    }
}