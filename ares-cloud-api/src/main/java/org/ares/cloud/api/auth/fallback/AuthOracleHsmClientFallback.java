package org.ares.cloud.api.auth.fallback;

import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.exception.ServiceUnavailableException;
import org.ares.cloud.api.auth.AuthOracleHsmClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class AuthOracleHsmClientFallback implements AuthOracleHsmClient {

    private static final String SERVICE_NAME = "ares-cloud-audit";

    @Override
    public String sign(@Validated(OcidHsmDto.SignGroup.class) @RequestBody OcidHsmDto ocidHsmDto) {
        throw new ServiceUnavailableException(SERVICE_NAME, "sign");
    }

    @Override
    public Boolean verify(@Validated(OcidHsmDto.Verify.class) @RequestBody OcidHsmDto ocidHsmDto) {
        throw new ServiceUnavailableException(SERVICE_NAME, "verify");
    }

    @Override
    public String encrypt(@Validated(OcidHsmDto.Encrypt.class) @RequestBody OcidHsmDto ocidHsmDto) {
        throw new ServiceUnavailableException(SERVICE_NAME, "encrypt");
    }

    @Override
    public String decrypt(@Validated(OcidHsmDto.Decrypt.class) @RequestBody OcidHsmDto ocidHsmDto) {
        throw new ServiceUnavailableException(SERVICE_NAME, "decrypt");
    }
}
