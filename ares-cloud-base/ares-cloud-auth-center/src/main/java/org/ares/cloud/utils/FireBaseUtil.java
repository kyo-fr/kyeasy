package org.ares.cloud.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.common.exception.BusinessException;
import org.ares.cloud.properties.FirebaseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author hugo  tangxkwork@163.com
 * @Description Firebase 工具类，从 Nacos 配置中心读取配置
 * @Date 2024/08/06/01:40
 **/
@Slf4j
@Component
public class FireBaseUtil {

    public static FirebaseApp firebaseApp;

    @Autowired
    private FirebaseProperties firebaseProperties;

    @PostConstruct
    public void init() {
        try {
            // 从 Nacos 配置构建 Firebase 配置 JSON
            // 使用 Map<String, String> 确保所有值都是字符串类型，避免类型转换错误
            Map<String, String> serviceAccountConfig = new HashMap<>();
            // 设置必需字段
            serviceAccountConfig.put("type", firebaseProperties.getType() != null ? firebaseProperties.getType() : "service_account");
            serviceAccountConfig.put("project_id", firebaseProperties.getProjectId() != null ? firebaseProperties.getProjectId() : "");
            serviceAccountConfig.put("private_key_id", firebaseProperties.getPrivateKeyId());
            serviceAccountConfig.put("private_key", firebaseProperties.getPrivateKey());
            serviceAccountConfig.put("client_email", firebaseProperties.getClientEmail());
            serviceAccountConfig.put("client_id", String.valueOf(firebaseProperties.getClientId()));
            serviceAccountConfig.put("auth_uri", firebaseProperties.getAuthUri());
            serviceAccountConfig.put("token_uri", firebaseProperties.getTokenUri());
            serviceAccountConfig.put("auth_provider_x509_cert_url", firebaseProperties.getAuthProviderX509CertUrl());
            serviceAccountConfig.put("client_x509_cert_url", firebaseProperties.getClientX509CertUrl());
            serviceAccountConfig.put("universe_domain", firebaseProperties.getUniverseDomain());
            // 将配置转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonConfig = objectMapper.writeValueAsString(serviceAccountConfig);
            // 将 JSON 字符串转换为 InputStream
            InputStream serviceAccount = new ByteArrayInputStream(jsonConfig.getBytes(StandardCharsets.UTF_8));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    //.setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
                    .build();

            firebaseApp = FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK 初始化成功，项目ID: {}", firebaseProperties.getProjectId());
        } catch (Exception e) {
            log.error("Firebase Admin SDK 初始化失败，请检查 Nacos 配置是否正确", e);
            throw new RuntimeException("Firebase Admin SDK 初始化失败", e);
        }
    }

    public static void main(String[] args) {
        try {
            UserRecord user = getUserById("jeRGWA0iyUVvedcfpxYGXFTuMxv2");
            System.out.println(user.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * token验证
     * @param idToken 通亨
     * @throws FirebaseAuthException 验证多雾
     */
    public static String verifyIdToken(String idToken) throws FirebaseAuthException{
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken.getUid();
    }

    /**
     * 获取用户信息
     * @param uid 用户id
     * @return  用户信息
     */
    public static UserRecord getUserById(String uid){
        FirebaseAuth instance = FirebaseAuth.getInstance(firebaseApp);
        UserRecord user = null;
        try {
            user = instance.getUserAsync(uid).get();
        } catch (Exception e) {
            throw new BusinessException("get user from firebase by uid error");
        }
        return user;
    }
}
