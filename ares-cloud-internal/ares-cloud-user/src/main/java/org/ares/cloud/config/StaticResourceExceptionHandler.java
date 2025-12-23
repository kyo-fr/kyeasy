package org.ares.cloud.config;

import jakarta.servlet.http.HttpServletRequest;
import org.ares.cloud.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 静态资源异常处理器
 * 静默处理静态资源未找到的异常，避免日志污染
 */
@ControllerAdvice
@Order(0) // 高优先级，在其他异常处理器之前处理
public class StaticResourceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceExceptionHandler.class);

    /**
     * 处理静态资源未找到异常
     * 对于静态资源请求（如 favicon.ico、空路径等），静默处理，不记录错误日志
     *
     * @param req 请求
     * @param e 异常
     * @return 返回空响应或 404
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNoResourceFoundException(HttpServletRequest req, NoResourceFoundException e) {
        String requestPath = req.getRequestURI();

        // 只对静态资源请求静默处理，其他请求记录警告
        if (isStaticResourceRequest(requestPath)) {
            // 静默处理，不记录日志
            return Result.success("");
        } else {
            // 非静态资源请求，记录警告
            log.warn("资源未找到: {}", requestPath);
            return Result.error("资源未找到: " + requestPath);
        }
    }

    /**
     * 判断是否为静态资源请求
     *
     * @param path 请求路径
     * @return 是否为静态资源请求
     */
    private boolean isStaticResourceRequest(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return true;
        }

        // 常见的静态资源路径
        return path.equals("/favicon.ico")
                || path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/img/")
                || path.endsWith(".ico")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".css")
                || path.endsWith(".js");
    }
}

