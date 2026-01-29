package org.ares.cloud.api.gateway;

import org.ares.cloud.api.gateway.fallback.GatewayPublicPathClientFallback;
import org.ares.cloud.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 网关公开路径 Feign 客户端，用于从 gateway 服务获取公开路径列表（不引入 gateway 依赖）
 */
@FeignClient(name = "ares-cloud-gateway", contextId = "gatewayPublicPathClient", configuration = FeignConfig.class, fallback = GatewayPublicPathClientFallback.class)
public interface GatewayPublicPathClient {

    /**
     * 获取 gateway 中配置的公开路径列表（默认 + 配置的 publicPaths）
     *
     * @return 公开路径列表
     */
    @GetMapping("/internal/gateway/public-paths")
    List<String> getPublicPaths();
}
