package org.ares.cloud.user.interceptor;

import com.alibaba.nacos.common.utils.CollectionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.ares.cloud.api.user.dto.SysRoleUrlDto;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.JsonUtils;
import org.ares.cloud.user.annotation.RequireUrlPermission;
import org.ares.cloud.user.entity.SysRoleUrlEntity;
import org.ares.cloud.user.entity.SysUserRoleEntity;
import org.ares.cloud.user.service.SysClassificationUrlService;
import org.ares.cloud.user.service.SysRoleUrlService;
import org.ares.cloud.user.service.SysUserRoleService;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hugo tangxkwork@163.com
 * @description URL权限拦截器，用于检查用户是否有访问指定URL的权限
 * @version 1.0.0
 * @date 2024-10-11
 */
@Component
public class UrlPermissionInterceptor implements HandlerInterceptor {

    private final SysClassificationUrlService sysClassificationUrlService;
    private final SysRoleUrlService sysRoleUrlService;
    private final SysUserRoleService sysUserRoleService;

    public UrlPermissionInterceptor(
            SysClassificationUrlService sysClassificationUrlService,
            SysRoleUrlService sysRoleUrlService,
            SysUserRoleService sysUserRoleService) {
        this.sysClassificationUrlService = sysClassificationUrlService;
        this.sysRoleUrlService = sysRoleUrlService;
        this.sysUserRoleService = sysUserRoleService;
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

        // 从ApplicationContext获取用户ID
        String userId = ApplicationContext.getUserId();
        if (StringUtils.isBlank(userId)) {
            // 用户未登录，返回401
            writeErrorResponse(response, ResponseCodeEnum.RECODE_TOKEN_BE_OVERDUE, "用户未登录");
            return false;
        }

        // 根据URL查询URL_ID
        String urlId = sysClassificationUrlService.getUrlIdByUrl(requestUrl);
        if (StringUtils.isBlank(urlId)) {
            // URL未在系统中配置，默认放行（或者可以根据业务需求返回错误）
            return true;
        }

        // 根据URL_ID查询关联的角色ID列表
        SysRoleUrlEntity entity = new SysRoleUrlEntity();
        entity.setUrlId(urlId);
        List<SysRoleUrlDto> urlDtoList = sysRoleUrlService.getRoleIdsByUrlIds(entity);
        if (CollectionUtils.isEmpty(urlDtoList)) {
            // URL未关联任何角色，默认放行（或者可以根据业务需求返回错误）
            return true;
        }

        // 根据用户ID查询用户关联的角色ID列表
        SysUserRoleEntity userRoleEntity = sysUserRoleService.getRoleIdsByUserId(userId);
        if (userRoleEntity == null) {
            // 用户没有角色，返回403
            writeErrorResponse(response, ResponseCodeEnum.RECODE_NOT_POWER, "您没有访问此URL的权限");
            return false;
        }

        // 判断用户角色是否在URL关联的角色中
        Set<String> urlRoles = urlDtoList.stream().map(SysRoleUrlDto::getRoleId).collect(Collectors.toSet());

        // 检查是否有交集
        boolean hasPermission = false;
        if (urlRoles.contains(userRoleEntity.getRoleId())) {
            hasPermission = true;
        }
        if (!hasPermission) {
            // 用户角色不在URL关联的角色中，返回403
            writeErrorResponse(response, ResponseCodeEnum.RECODE_NOT_POWER, "您没有访问此URL的权限");
            return false;
        }

        // 权限验证通过，放行
        return true;
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

