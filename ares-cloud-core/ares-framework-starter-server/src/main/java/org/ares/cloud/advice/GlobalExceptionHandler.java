package org.ares.cloud.advice;


import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.exception.*;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.i18n.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hugo
 * @version 1.0
 * @description: TODO
 * @date 2024/9/29 14:30
 */
@ControllerAdvice
@Order(1)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     *   处理自定义的业务异常
     * @param req 请求
     * @param e 异常
     * @return 返回错误响应
     */
    @ExceptionHandler
            (value = BusinessException.class)
    @ResponseBody
    public Result<String> businessExceptionHandler(HttpServletRequest req, BusinessException e){
        log.error("发生业务异常！原因是：",e);
        return this.buildResult(e,e.getMsg());
    }

    /**
     * 处理空指针的异常
     * @param req 请求
     * @param e 异常
     * @return 返回错误响应
     */
    @ExceptionHandler(value =NullPointerException.class)
    @ResponseBody
    public Result<String> exceptionHandler(HttpServletRequest req, NullPointerException e){
        //log.error("发生业务异常！原因是：",e.getMessage());
        log.error("发生业务异常！原因是：",e);
        return Result.ResultEnumErr(ResponseCodeEnum.RECODE_UNKNOWN_ERR,e.getMessage());
    }


    /**
     * 处理其他异常
     * @param req 请求
     * @param e 异常
     * @return 返回错误响应
     */
    @ExceptionHandler(value =Exception.class)
    @ResponseBody
    public Result<String> exceptionHandler(HttpServletRequest req, Exception e){
        log.error("发生业务异常！原因是：",e);
        if(StringUtils.isBlank(e.getMessage())){
            return Result.ResultEnumErr(ResponseCodeEnum.RECODE_ERR,e.getMessage());
        }
        return  Result.error(e.getMessage());
    }

    /**
     * 权限异常处理
     * @param req 请求
     * @param e 异常
     * @return 返回错误响应
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseBody
    public Result<String> accessDeniedExceptionHandler(HttpServletRequest req, Exception e){
        log.error("发生业务异常！原因是：",e);
        if(StringUtils.isBlank(e.getMessage())){
            return Result.ResultEnumErr(ResponseCodeEnum.RECODE_NOT_POWER,e.getMessage());
        }
        return  Result.error(e.getMessage());
    }

    /**
     *   处理自定义的业务异常
     */
    @ExceptionHandler(value = FileUploadException.class)
    @ResponseBody
    public Result<String> bizExceptionHandler(HttpServletRequest req, FileUploadException e){
        log.error("发生业务异常！原因是："+e);
        return buildResult(e,e.getException());
    }
    /**
     *   基础异常
     */
    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public Result<String> bizExceptionHandler(HttpServletRequest req, BaseException e){
        if (e.getException() != null) {
            log.error("发生异常！原因是："+e.getException());
        }else {
            log.error("发生异常！原因是："+e);
        }
        return buildResult(e,e.getException());
    }
    /**
     *   基础异常
     */
    @ExceptionHandler(value = CaptchaException.class)
    @ResponseBody
    public Result<String> bizExceptionHandler(HttpServletRequest req, CaptchaException e){
        log.error("发生异常！原因是："+e);
        return buildResult(e,e.getException());
    }
    /**
     *   基础异常
     */
    @ExceptionHandler(value = DaoException.class)
    @ResponseBody
    public Result<String> bizExceptionHandler(HttpServletRequest req, DaoException e){
        log.error("发生异常！原因是："+e);
        return buildResult(e,e.getException());
    }
    /**
     *   基础异常
     */
    @ExceptionHandler(value = RequestBadException.class)
    @ResponseBody
    public Result<String> bizHandler(HttpServletRequest req, RequestBadException e){
        log.error("发生异常！请求错误原因是："+e);
        // RequestBadException 实现了 BaseErrorInfoInterface，使用 getMessageKey() 获取国际化消息
        return buildResult(e, (String) null);
    }

    /**
     *   基础异常
     */
    @ExceptionHandler(value = LockException.class)
    @ResponseBody
    public Result<String> bizExceptionHandler(HttpServletRequest req, LockException e){
        log.error("发生异常！原因是："+e);
        return buildResult(e,e.getException());
    }
    /**
     *   基础异常
     */
    @ExceptionHandler(value = TokenException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 设置 HTTP 状态码为 401
    public Result<String> bizExceptionHandler(HttpServletRequest req, TokenException e){
        log.error("发生异常！原因是："+e);
        return buildResult(e,e.getException());
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseBody
    public Result<String> bizNotFoundExceptionHandler(HttpServletRequest req, NotFoundException e){
        log.error("发生异常！原因是："+e);
        return buildResult(e,e.getException());
    }
    /**
     *   参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<String> handleValidationExceptions(HttpServletRequest request,MethodArgumentNotValidException ex){
        StringBuilder  strErr =  new StringBuilder("[");
        // 遍历字段错误
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();

            String messageKey = extractTextWithinBraces(message);
            if(StringUtils.isNotBlank(messageKey)){
                Object[] arguments = fieldError.getArguments();
                if(arguments != null && arguments.length > 1){
                    // 调用 MessageUtils 获取自定义错误信息
                    message = MessageUtils.get(messageKey,removeFirstElement(arguments), message);
                }else{
                    message = MessageUtils.get(messageKey, message);
                }
            }
            // 拼接错误信息，不在最后一个错误后加逗号
            strErr.append(fieldName).append(":").append(message);

            // 如果不是最后一个错误，添加逗号
            if (i < fieldErrors.size() - 1) {
                strErr.append(",");
            }
        }
        strErr.append("]");
        return new Result<>(HttpStatus.BAD_REQUEST.value(),strErr.toString(),"");
    }

    /**
     * 构建错误
     * @param err 业务异常
     * @param e 异常
     * @return 通用模型
     */
    private  Result<String> buildResult(BaseErrorInfoInterface err,Exception e){
        Result<String> result =  Result.error(err,e);
        if(null != result.getReason()){
            String i18nMsg = MessageUtils.get(err.getMessageKey(),null);
            // 确保消息被正确设置，如果国际化消息为空或为"NOT_TRANSLATED"，使用消息键作为默认消息
            if(StringUtils.isNotBlank(i18nMsg) && !"NOT_TRANSLATED".equals(i18nMsg)){
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
     * 构建错误
     * @param err 异常
     * @return 通模型
     */
    private  Result<String> buildResult(BaseErrorInfoInterface err,String defMsg){
        Result<String> result =  Result.error(err);
        if(null != err.getMessageKey()){
            String i18nMsg = MessageUtils.get(err.getMessageKey(),defMsg);
            // 确保消息被正确设置，如果国际化消息为空或为"NOT_TRANSLATED"，使用默认消息
            if(StringUtils.isNotBlank(i18nMsg) && !"NOT_TRANSLATED".equals(i18nMsg)){
                result.setMsg(i18nMsg);
                result.setReason(i18nMsg);
            } else if(StringUtils.isNotBlank(defMsg)){
                result.setMsg(defMsg);
                result.setReason(defMsg);
            } else {
                // 如果都没有，使用消息键作为默认消息
                result.setMsg(err.getMessageKey());
                result.setReason(err.getMessageKey());
            }
        } else if(StringUtils.isNotBlank(defMsg)){
            // 如果没有国际化key，直接使用默认消息
            result.setMsg(defMsg);
            result.setReason(defMsg);
        }
        return result;
    }

    /**
     * 获取国际化的key
     * @param text temp
     * @return key
     */
    public  String extractTextWithinBraces(String text) {
        // 创建一个正则表达式模式，严格匹配以 { 开头，以 } 结尾的字符串
        Pattern pattern = Pattern.compile("^\\{(.*?)\\}$");
        Matcher matcher = pattern.matcher(text);

        // 如果匹配成功，则返回大括号中的内容；否则返回空串
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
    public  Object[] removeFirstElement(Object[] originalArray) {
        // 如果数组为空或者只有一个元素，返回空数组
        if (originalArray == null || originalArray.length == 0) {
            return new Object[0];
        }

        // 创建一个新的数组，长度比原数组小 1
        Object[] newArray = new Object[originalArray.length - 1];

        // 将原数组从第二个元素开始复制到新数组
        System.arraycopy(originalArray, 1, newArray, 0, newArray.length);

        return newArray;
    }
}
