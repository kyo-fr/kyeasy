package org.ares.cloud.api.gateway.fallback;

import org.ares.cloud.api.gateway.GatewayPublicPathClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 网关公开路径 Feign 客户端降级处理
 */
@Component
public class GatewayPublicPathClientFallback implements GatewayPublicPathClient {

    @Override
    public List<String> getPublicPaths() {
        return Collections.emptyList();
    }
}
