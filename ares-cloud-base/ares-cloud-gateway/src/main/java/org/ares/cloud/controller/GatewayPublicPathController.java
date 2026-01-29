package org.ares.cloud.controller;

import org.ares.cloud.properties.GatewayProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 网关公开路径接口，供其他服务通过 Feign 获取公开路径列表（不做鉴权）
 */
@RestController
@RequestMapping("/internal/gateway")
public class GatewayPublicPathController {

    private final GatewayProperties gatewayProperties;

    public GatewayPublicPathController(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    /**
     * 获取公开的路径列表（默认 + 配置的 publicPaths）
     */
    @GetMapping("/public-paths")
    public List<String> getPublicPaths() {
        return gatewayProperties.getPublicPaths();
    }
}
