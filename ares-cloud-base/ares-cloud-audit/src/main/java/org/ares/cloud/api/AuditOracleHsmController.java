package org.ares.cloud.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.ares.cloud.api.auth.dto.OcidHsmDto;
import org.ares.cloud.service.OracleHsmService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit/oracle/hsm/v1")
@Tag(name = "oracle HSM服务")
public class AuditOracleHsmController {

    @Resource
    private OracleHsmService oracleHsmService;

    @PostMapping(value = "/sign")
    public String sign(@Validated(OcidHsmDto.SignGroup.class) @RequestBody OcidHsmDto ocidHsmDto) throws Exception {
        return oracleHsmService.sign(ocidHsmDto.getData());
    }

    @PostMapping(value = "/verify")
    public Boolean verify(@Validated(OcidHsmDto.Verify.class) @RequestBody OcidHsmDto ocidHsmDto) throws Exception {
        return oracleHsmService.verify(ocidHsmDto.getData(), ocidHsmDto.getSignature());
    }

    @PostMapping(value = "/encrypt")
    public String encrypt(@Validated(OcidHsmDto.Encrypt.class) @RequestBody OcidHsmDto ocidHsmDto) throws Exception {
        return oracleHsmService.encrypt(ocidHsmDto.getPlaintext());
    }

    @PostMapping(value = "/decrypt")
    public String decrypt(@Validated(OcidHsmDto.Decrypt.class) @RequestBody OcidHsmDto ocidHsmDto) throws Exception {
        return oracleHsmService.decryptToString(ocidHsmDto.getCiphertext());
    }

}

