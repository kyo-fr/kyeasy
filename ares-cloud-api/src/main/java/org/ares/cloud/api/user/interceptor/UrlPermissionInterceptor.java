package org.ares.cloud.api.user.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.gateway.GatewayPublicPathClient;
import org.ares.cloud.api.user.UserPermissionClient;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.api.user.dto.SysUserRoleDto;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.JsonUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author hugo tangxkwork@163.com
 * @description URL权限拦截器，用于检查用户是否有访问指定URL的权限
 * @version 1.0.0
 * @date 2024-10-11
 */
public class UrlPermissionInterceptor implements HandlerInterceptor {

    private final UserPermissionClient userPermissionClient;
    private final GatewayPublicPathClient gatewayPublicPathClient;

    public UrlPermissionInterceptor(UserPermissionClient userPermissionClient,
                                    GatewayPublicPathClient gatewayPublicPathClient) {
        this.userPermissionClient = userPermissionClient;
        this.gatewayPublicPathClient = gatewayPublicPathClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法或类上是否有@RequireUrlPermission注解
        RequireUrlPermission annotation = handlerMethod.getMethodAnnotation(RequireUrlPermission.class);
        if (annotation == null) {
            // 检查类上的注解
            annotation = handlerMethod.getBeanType().getAnnotation(RequireUrlPermission.class);
        }

        // 如果没有注解或注解值为false，则跳过权限检查
        if (annotation == null || !annotation.value()) {
            return true;
        }

        // 获取请求URL（去除查询参数）
        String requestUrl = request.getRequestURI();
        if (StringUtils.isBlank(requestUrl)) {
            return true;
        }

        // 通过 Feign 调用 gateway 服务获取公开路径，若当前请求为公开路径则直接放行
        List<String> publicPaths = gatewayPublicPathClient.getPublicPaths();
        if (CollectionUtil.isNotEmpty(publicPaths)) {
            for (String pattern : publicPaths) {
                if (StringUtils.isNotBlank(pattern) && pattern.contains(requestUrl)) {
                    return true;
                }
            }
        }

        // 获取请求方法（GET、POST等）
        String requestMethod = request.getMethod();

        // 从ApplicationContext获取用户ID
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isBlank(userId)) {
            // 用户未登录，返回401
            writeErrorResponse(response, ResponseCodeEnum.RECODE_TOKEN_BE_OVERDUE, "用户未登录");
            return false;
        }

        // 通过Feign调用user服务获取用户角色
        SysUserRoleDto userRoleDto = userPermissionClient.getRoleByUserId(userId);
        if (userRoleDto == null || StringUtils.isBlank(userRoleDto.getRoleId())) {
            // 用户没有角色，返回403
            writeErrorResponse(response, ResponseCodeEnum.RECODE_NOT_POWER, "您没有访问此URL的权限");
            return false;
        }

        // 通过Feign调用user服务获取角色关联的URL列表
        String roleId = userRoleDto.getRoleId();
        List<SysClassificationUrlDto> urlDtoList = userPermissionClient.getUrlsByRoleId(roleId);

        // 如果查询为空，返回false
        if (CollectionUtil.isEmpty(urlDtoList)) {
            writeErrorResponse(response, ResponseCodeEnum.RECODE_NOT_POWER, "您没有访问此URL的权限");
            return false;
        }

        // 遍历URL名称，与requestUrl进行匹配
        if (CollectionUtil.isNotEmpty(urlDtoList)) {
            for (SysClassificationUrlDto urlDto : urlDtoList) {
                String urlName = urlDto.getUrl();
                if (StringUtils.isNotBlank(urlName) && requestUrl.contains(urlName)
                        && requestMethod.equals(urlDto.getMethodType())) {
                    // 匹配成功，返回true
                    return true;
                }
            }
        }

        // 没有匹配的URL，返回false
        writeErrorResponse(response, ResponseCodeEnum.RECODE_NOT_POWER, "您没有访问此URL的权限");
        return false;
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, ResponseCodeEnum errorCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Result<String> result = Result.error(errorCode.getCode(), message);

        PrintWriter writer = response.getWriter();
        writer.write(JsonUtils.toJsonString(result));
        writer.flush();
        writer.close();
    }
}

