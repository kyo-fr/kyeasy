package org.ares.cloud.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RefreshScope
@ConfigurationProperties(prefix = "ares.gateway")
public class GatewayProperties {
    // 默认的公开路径
    private static final List<String> DEFAULT_PUBLIC_PATHS = Arrays.asList(
            "/favicon.ico",
            "/v3/api-docs/*",
            "/webjars/**",
            "/doc.html",
            "/*/v3/api-docs",
            "/*/api-docs/v3"
    );
    /**
     * 公开的url
     */
    private List<String> publicPaths;
    /**
     * 认证的地址
     */
    private String authUrl;


    /**
     * 获取公开的路径
     * @return 公开路径
     */
    public List<String> getPublicPaths() {
        // 合并用户配置的 publicPaths 和默认的 publicPaths
        List<String> combinedPaths = new ArrayList<>(DEFAULT_PUBLIC_PATHS);
        if (publicPaths != null) {
            combinedPaths.addAll(publicPaths);
        }
        return combinedPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
}
