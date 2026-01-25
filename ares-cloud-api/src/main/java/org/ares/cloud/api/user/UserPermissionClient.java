package org.ares.cloud.api.user;

import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.api.user.dto.SysUserRoleDto;
import org.ares.cloud.api.user.fallback.UserPermissionClientFallback;
import org.ares.cloud.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户权限服务客户端，用于权限校验
 */
@FeignClient(name = "user-service", contextId = "userPermissionClient", configuration = FeignConfig.class, fallback = UserPermissionClientFallback.class)
public interface UserPermissionClient {

    /**
     * 根据用户ID获取用户角色（用于权限校验）
     * @param userId 用户ID
     * @return 用户角色信息
     */
    @GetMapping("/internal/user/v1/users/permission/role")
    SysUserRoleDto getRoleByUserId(@RequestParam("userId") String userId);

    /**
     * 根据角色ID获取URL列表（用于权限校验）
     * @param roleId 角色ID
     * @return URL列表
     */
    @GetMapping("/internal/user/v1/users/permission/urls")
    List<SysClassificationUrlDto> getUrlsByRoleId(@RequestParam("roleId") String roleId);
}

