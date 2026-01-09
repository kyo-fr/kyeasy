package org.ares.cloud.common.exception;

import org.ares.cloud.common.enums.ResponseCodeEnum;

import java.io.Serial;

/**
 * 请求参数问题
 */
public class RequestBadException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;


    public RequestBadException() {
        super();
    }

    public RequestBadException(BaseErrorInfoInterface err) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(), null, err.getMessageKey());
    }

    public RequestBadException(BaseErrorInfoInterface err, Throwable cause) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(), null, err.getMessageKey());
    }
    public RequestBadException(BaseErrorInfoInterface err, Exception exception) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(), null, err.getMessageKey());
        this.exception = exception;
    }

    public RequestBadException(DefMessageErrorInterface err) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(),err.getMsg(), err.getMessageKey());
    }

    public RequestBadException(DefMessageErrorInterface err, Throwable cause) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(),err.getMsg(), err.getMessageKey());
    }
    public RequestBadException(DefMessageErrorInterface err, Exception exception) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(),err.getMsg(), err.getMessageKey());
        this.exception = exception;
    }
    /**
     * 默认的异常
     */
    public RequestBadException(String msg) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(),msg, ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getMessageKey());
    }

    /**
     * 默认的异常
     */
    public RequestBadException(Exception e) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR,e);
    }
    public RequestBadException(Integer code, String msg) {
        super(code,msg);
    }

    public RequestBadException(String msg, String msgKey) {
        super(ResponseCodeEnum.REQUEST_PARAMETER_ERROR.getCode(),msg,msgKey);
    }
}
