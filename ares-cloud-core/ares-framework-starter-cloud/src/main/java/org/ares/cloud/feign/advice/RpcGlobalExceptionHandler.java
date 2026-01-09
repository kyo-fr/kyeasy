package org.ares.cloud.feign.advice;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.common.exception.BaseErrorInfoInterface;
import org.ares.cloud.common.exception.BaseException;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.ExceptionUtils;
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.i18n.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author hugo
 * @version 1.0
 * @description: RPC 全局异常处理器
 * 专门处理 Feign RPC 调用过程中产生的异常，不影响正常对外接口的异常处理
 * 只处理 RpcCallException 和 FeignException，其他异常交给 GlobalExceptionHandler 处理
 * 确保返回标准的 Result 格式，异常上下文保存在 exception 字段中
 * 支持国际化处理，使用 MessageUtils 翻译 messageKey
 * @date 2024/10/16 15:46
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，确保在全局异常处理器之前执行
public class RpcGlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(RpcGlobalExceptionHandler.class);
    
    /**
     * 构建错误响应并处理国际化
     * @param err 错误接口
     * @param exception 内部异常
     * @return Result
     */
    private Result<String> buildResult(BaseErrorInfoInterface err, Exception exception) {
        Result<String> result = Result.error(err, exception);
        // 国际化处理：使用 messageKey 获取翻译后的消息
        if (result.getReason() != null) {
            String i18nMsg = MessageUtils.get(err.getMessageKey(), null);
            // 确保消息被正确设置，如果国际化消息为空或为"NOT_TRANSLATED"，使用消息键作为默认消息
            if (StringUtils.isNotBlank(i18nMsg) && !"NOT_TRANSLATED".equals(i18nMsg)) {
                result.setMsg(i18nMsg);
                result.setReason(i18nMsg);
            } else {
                // 如果国际化消息获取失败，使用消息键作为默认消息
                result.setMsg(err.getMessageKey());
                result.setReason(err.getMessageKey());
            }
        }
        return result;
    }
    
    /**
     * 构建错误响应并处理国际化
     * @param err 错误接口
     * @return Result
     */
    private Result<String> buildResult(BaseErrorInfoInterface err) {
        return buildResult(err, null);
    }

    /**
     * 处理 RPC 调用异常
     * 将远程服务的完整错误信息传递给调用方
     * 异常上下文信息保存在 Result 的 exception 字段中
     * 
     * @param req 请求
     * @param e RPC 调用异常
     * @return 返回包含完整错误上下文的 Result
     */
    @ExceptionHandler(value = RpcCallException.class)
    @ResponseBody
    public Result<String> handleRpcCallException(HttpServletRequest req, RpcCallException e) {
        Result<String> result;
        
        // 如果是从远程 Result 构建的 RpcCallException
        if (e.isFromRemoteResult()) {
            // 使用 buildResult 处理国际化
            result = buildResult(e, e.getWrappedException());
            
            // 将远程异常上下文保存到 Result 的 exception 字段
            String remoteException = e.getRemoteExceptionContext();
            if (remoteException != null && !remoteException.isEmpty()) {
                result.setException(remoteException);
            }
            
            log.warn("RPC 调用返回远程异常: code={}, msg={}, reason={}, exception={}", 
                    e.getCode(), e.getMsg(), e.getMessageKey(), remoteException);
            
        } else {
            // 包装了本地异常
            Exception wrappedException = e.getWrappedException();
            
            if (wrappedException instanceof BaseErrorInfoInterface baseErrorInfo) {
                // 业务异常，使用 buildResult 处理国际化
                if (wrappedException instanceof BaseException baseException) {
                    result = buildResult(baseErrorInfo, baseException.getException());
                    
                    Exception innerException = baseException.getException();
                    if (innerException != null) {
                        // 如果有内部异常，追加到 exception 字段
                        String fullException = e.getFullExceptionMessage();
                        result.setException(fullException);
                        log.warn("RPC 调用包装业务异常(含内部异常): code={}, reason={}, innerException={}", 
                                baseErrorInfo.getCode(), 
                                baseErrorInfo.getMessageKey(),
                                innerException.getClass().getSimpleName());
                    } else {
                        log.warn("RPC 调用包装业务异常: code={}, reason={}", 
                                baseErrorInfo.getCode(), 
                                baseErrorInfo.getMessageKey());
                    }
                } else {
                    result = buildResult(baseErrorInfo);
                    result.setException(ExceptionUtils.getExceptionMessage(wrappedException));
                }
            } else {
                // 技术异常
                result = buildResult(e);
                result.setException(e.getFullExceptionMessage());
                log.error("RPC 调用发生技术异常: {}", e.getFullExceptionMessage());
            }
        }
        
        return result;
    }

    /**
     * 处理 Feign 原生异常（FeignException）
     * 这些是 Feign 框架层面的异常，通常是网络、超时等技术问题
     * 特别处理 FeignServerException，可能包装了 BaseException
     * 
     * @param req 请求
     * @param e Feign 异常
     * @return 返回错误响应
     */
    @ExceptionHandler(value = FeignException.class)
    @ResponseBody
    public Result<String> handleFeignException(HttpServletRequest req, FeignException e) {
        // 检查是否是 FeignServerException 且包装了 BaseException
        if (e instanceof FeignException.FeignServerException) {
            Throwable cause = e.getCause();
            if (cause instanceof BaseException baseException) {
                // 从 FeignResponseDecoder 抛出的，包含了远程服务的完整错误信息
                // 使用 buildResult 处理国际化
                Result<String> result = buildResult(baseException, baseException.getException());
                
                log.warn("RPC 调用返回业务错误(HTTP 200): code={}, msg={}, reason={}, exception={}", 
                        baseException.getCode(), 
                        result.getMsg(),  // 国际化后的消息
                        baseException.getMessageKey(),
                        result.getException());
                
                return result;
            }
        }
        
        // 普通的 FeignException（网络超时、连接失败等技术问题）
        log.error("Feign 调用发生技术异常: status={}, message={}", e.status(), e.getMessage(), e);
        
        int errorCode = e.status() > 0 ? e.status() : HttpStatus.SERVICE_UNAVAILABLE.value();
        String errorMessage = "远程服务调用失败";
        
        BaseException baseException = new BaseException(errorCode, errorMessage, "rpc_feign_error");
        Result<String> result = buildResult(baseException);
        
        // 将完整的 Feign 异常信息保存在 exception 字段中
        result.setException(ExceptionUtils.getExceptionMessage(e));
        
        return result;
    }
}

