package org.ares.cloud.api.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hugo tangxkwork@163.com
 * @description URL权限检查注解，用于标记需要权限验证的Controller方法
 * @version 1.0.0
 * @date 2024-10-11
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUrlPermission {
    /**
     * 是否启用权限检查，默认为true
     * @return true表示需要权限检查，false表示跳过权限检查
     */
    boolean value() default true;
}

