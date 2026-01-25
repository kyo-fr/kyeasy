package org.ares.cloud.api.user.fallback;

import org.ares.cloud.api.user.UserPermissionClient;
import org.ares.cloud.api.user.dto.SysClassificationUrlDto;
import org.ares.cloud.api.user.dto.SysUserRoleDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 用户权限服务客户端降级处理
 */
@Component
public class UserPermissionClientFallback implements UserPermissionClient {

    @Override
    public SysUserRoleDto getRoleByUserId(String userId) {
        // 降级时返回null，拦截器会处理
        return null;
    }

    @Override
    public List<SysClassificationUrlDto> getUrlsByRoleId(String roleId) {
        // 降级时返回空列表，拦截器会处理
        return Collections.emptyList();
    }
}

