package org.ares.cloud.api.auth;

import org.ares.cloud.api.auth.fallback.AuthServerClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author hugo
 * @version 1.0
 * @description: TODO
 * @date 2024/9/23 17:06
 */
@FeignClient(name = "ares-cloud-auth-center", contextId = "authServerClient", fallback = AuthServerClientFallback.class)
public interface AuthServerClient {
    /**
     * 验证token
     * @param token 令牌
     * @return 解析结果
     */
    @PostMapping("/auth/validate")
    CompletableFuture<ResponseEntity<Map<String, Object>>> validateToken(@RequestBody String token);
}
