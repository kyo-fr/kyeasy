package org.ares.cloud.platformInfo.enums;

import org.ares.cloud.common.exception.BaseErrorInfoInterface;
import org.springframework.http.HttpStatus;

/**
 * @author hugo
 * @version 1.0
 * @description: 认证的错误
 * @date 2024/9/30 18:46
 */
public enum PlatformError implements BaseErrorInfoInterface {
    PLATFORM_MERCHANT_TYPE_EXIST_ERROR("platform.merchant.type.exist.error"),
    PLATFORM_MARKING_EXIST_ERROR("platform.marking.markingName.exist.error"),//标注名称已存在
    PLATFORM_PAY_WAY_NOT_EXIST_ERROR("platform.pay.way.not.exist.error"),//平台支付类型不存在
    PLATFORM_SUBSCRIBE_EXIST_ERROR("platform.subscribe.is.exist.error"),//平台订阅类型已存在
    PLATFORM_USER_APPROVAL_EXIST_ERROR("platform.user.openMerchant.approval.is.exist.error"),//该用户已存在开通商户类型的审批单
    PLATFORM_USER_APPROVAL_NOT_EXIST_ERROR("platform.user.approval.not.exist.error"),//该用户不存在审批单
    PLATFORM_USER_INFO_NOT_EXIST_ERROR("platform.user.info.not.exist.error"),//用户信息不存在
    PLATFORM_USER_APPROVAL_NOT_PASS_ERROR("platform.user.approval.not.pass.error"),//审批单未审核通过
    PLATFORM_USER_APPROVAL_PASS_ERROR("platform.user.approval.pass.error"),//审批单已经审核通过
    PLATFORM_MERCHANT_ID_NOT_EXIST_ERROR("validation.tenantId.notBlank"),//商户字段不存在
    PLATFORM_MERCHANT_PRODUCT_TAX_RATE_EXCEED_ERROR("platform.product.tax.rate.exceed.error"),//#商户设置税率超过10条异常
    PLATFORM_TAX_TATE_TYPE_EXIST_ERROR("platform.taxTate.type.exist.error"),//税率类型已存在
    PLATFORM_MERCHANT_PRODUCT_IS_ENABLE_ERROR("platform.merchant.product.is.enable.error"),//该商户下的商品未全部下架
    PLATFORM_MERCHANT_ORDER_UNSETTLED_ERROR("platform.merchant.order.is.unsettled.error"),//该商户下存在未结算或部分结算的订单
    PLATFORM_USER_APPROVAL_AMOUNT_ERROR("platform.user.approval.amount.error"),//审批单金额异常
    PLATFORM_SUBSCRIBE_NOT_EXIST_ERROR("platform.subscribe.not.exist.error"),//平台订阅主数据不存在
    PLATFORM_USER_IS_MERCHANT_ERROR("platform.user.is.merchant.error"),//该用户已是商户无需查询
    PLATFORM_USER_IS_NOT_MERCHANT_ERROR("platform.user.is.not.merchant.error"),//该用户不是商户无法购买存储
    PLATFORM_USER_APPROVAL_NOT_SUB_ERROR("platform.user.approval.not.sub.error"),//该用户不存在可用(sub订阅中)审批单
    PLATFORM_USER_APPROVAL_ENOUGH_ERROR("platform.user.approval.enough.error"),//当前存储已消耗完成,该用户可用存储不足
    PLATFORM_INFO_NOT_EXIST_ERROR("platform.info.not.exist.error"),//平台信息不存在
    PLATFORM_ACCOUNT_PERMISSIONS_ERROR("platform.account.permissions.error"),//账号权限不足
    PLATFORM_TAX_NUM_EXIST_ERROR("platform.tax.num.exist.error"),//税务号已存在
    PLATFORM_MERCHANT_TENANTID_EXIST_ERROR("platform.merchant.tenantId.exist.error")//此手机号已注册商户，不可重复注册

    ;

    private final Integer code;
    private final String messageKey;


    PlatformError(Integer code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
    PlatformError(String message) {
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.messageKey = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
