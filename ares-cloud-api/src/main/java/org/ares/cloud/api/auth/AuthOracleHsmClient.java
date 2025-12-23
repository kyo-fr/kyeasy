package org.ares.cloud.api.auth;

import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.api.auth.fallback.AuthOracleHsmClientFallback;
import org.ares.cloud.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ares-cloud-auth-center", contextId = "authOracleHsmClient", configuration = FeignConfig.class, fallback = AuthOracleHsmClientFallback.class)
public interface AuthOracleHsmClient {

    /**
     * 生成签名
     */
    @PostMapping("/api/auth/oracle/hsm/v1/sign")
    String sign(@Validated(OcidHsmDto.SignGroup.class) @RequestBody OcidHsmDto ocidHsmDto);

    /**
     * 验签
     */
    @PostMapping("/api/auth/oracle/hsm/v1/verify")
    Boolean verify(@Validated(OcidHsmDto.Verify.class) @RequestBody OcidHsmDto ocidHsmDto);

    /**
     * 加密
     */
    @PostMapping("/api/auth/oracle/hsm/v1/encrypt")
    String encrypt(@Validated(OcidHsmDto.Encrypt.class) @RequestBody OcidHsmDto ocidHsmDto);

    /**
     * 解密
     */
    @PostMapping("/api/auth/oracle/hsm/v1/decrypt")
    String decrypt(@Validated(OcidHsmDto.Decrypt.class) @RequestBody OcidHsmDto ocidHsmDto);

}
