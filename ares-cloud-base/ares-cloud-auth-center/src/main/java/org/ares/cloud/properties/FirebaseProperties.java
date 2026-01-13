package org.ares.cloud.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Firebase Admin SDK 配置实体类
 * 绑定 Nacos 中 ares.cloud.auth.firebase.admin 节点的配置
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "firebase.admin")
public class FirebaseProperties {

    private String type;

    private String projectId;

    private String privateKeyId;

    private String privateKey;

    private String clientEmail;

    private String clientId;

    private String authUri;

    private String tokenUri;

    private String authProviderX509CertUrl;

    private String clientX509CertUrl;

    private String universeDomain;

}
