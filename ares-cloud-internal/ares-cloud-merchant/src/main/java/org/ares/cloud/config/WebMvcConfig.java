package org.ares.cloud.config;

import org.ares.cloud.api.user.UserPermissionClient;
import org.ares.cloud.api.user.interceptor.UrlPermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 处理静态资源请求和注册URL权限拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserPermissionClient userPermissionClient;

    public WebMvcConfig(@Lazy UserPermissionClient userPermissionClient) {
        this.userPermissionClient = userPermissionClient;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理 favicon.ico 请求
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // 处理其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册URL权限拦截器
        UrlPermissionInterceptor urlPermissionInterceptor = new UrlPermissionInterceptor(userPermissionClient);
        registry.addInterceptor(urlPermissionInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/error",           // 错误页面
                        "/favicon.ico",     // 图标
                        "/static/**",       // 静态资源
                        "/swagger-ui/**",   // Swagger UI
                        "/v3/api-docs/**", // API文档
                        "/swagger-resources/**", // Swagger资源
                        "/webjars/**"       // WebJars资源
                );
    }
}

