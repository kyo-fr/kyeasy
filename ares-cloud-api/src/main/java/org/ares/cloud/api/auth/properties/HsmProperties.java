package org.ares.cloud.api.auth.properties;

import lombok.Data;
import org.ares.cloud.common.constant.interfaces.AresConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Oracle HSM 配置属性
 *
 * @author system
 * @version 1.0
 * @description: Oracle HSM/KMS 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = HsmProperties.PREFIX)
public class HsmProperties {
    /**
     * 配置前缀
     */
    public static final String PREFIX = AresConstant.PROPERTIES_PREFIX + ".hsm";

    /**
     * OCI 配置文件路径，默认为 ~/.oci/config
     */
    private String configFilePath;

    /**
     * OCI 配置配置文件中的配置文件名，默认为 DEFAULT
     */
    private String configProfile;

    /**
     * 公钥
     */
    private String publicKeyOcid;

    /**
     * 密钥 OCID
     */
    private String keyOcid;

    /**
     * 密钥版本 OCID
     */
    private String keyVersionOcid;

    /**
     * Vault 名称
     */
    private String vaultName;

    /**
     * 保护模式：HSM
     */
    private String protectionMode;

    /**
     * 区域，从 keyOcid 中提取，默认为 eu-paris-1
     */
    private String region;

    /**
     * 是否启用 HSM
     */
    private boolean enabled;
}

