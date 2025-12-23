package org.ares.cloud.service.impl;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.keymanagement.KmsCryptoClient;
import com.oracle.bmc.keymanagement.model.DecryptDataDetails;
import com.oracle.bmc.keymanagement.model.EncryptDataDetails;
import com.oracle.bmc.keymanagement.model.SignDataDetails;
import com.oracle.bmc.keymanagement.model.VerifyDataDetails;
import com.oracle.bmc.keymanagement.requests.DecryptRequest;
import com.oracle.bmc.keymanagement.requests.EncryptRequest;
import com.oracle.bmc.keymanagement.requests.SignRequest;
import com.oracle.bmc.keymanagement.requests.VerifyRequest;
import com.oracle.bmc.keymanagement.responses.DecryptResponse;
import com.oracle.bmc.keymanagement.responses.EncryptResponse;
import com.oracle.bmc.keymanagement.responses.SignResponse;
import com.oracle.bmc.keymanagement.responses.VerifyResponse;
import com.oracle.bmc.model.BmcException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.service.OracleHsmService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Oracle HSM 服务实现类
 *
 * @author system
 * @version 1.0
 * @description: Oracle HSM/KMS 服务实现
 */
@Slf4j
@Service
public class OracleHsmServiceImpl implements OracleHsmService {

    @Resource
    private HsmProperties hsmProperties;

    private ConfigFileAuthenticationDetailsProvider authenticationProvider;
    private KmsCryptoClient kmsCryptoClient;

    @PostConstruct
    public void init() {
        if (!hsmProperties.isEnabled()) {
            log.warn("Oracle HSM is disabled, skipping initialization");
            return;
        }

        try {
            // 处理配置文件路径，展开 ~ 符号
            String configFilePath = hsmProperties.getConfigFilePath();
            if (configFilePath != null && configFilePath.startsWith("~")) {
                String homeDir = System.getProperty("user.home");
                configFilePath = configFilePath.replaceFirst("^~", homeDir);
            }

            // 初始化 OCI 认证提供者
            authenticationProvider = new ConfigFileAuthenticationDetailsProvider(
                    configFilePath,
                    hsmProperties.getConfigProfile()
            );

            // 从 key OCID 中提取 vault ID
            // Key OCID 格式: ocid1.key.oc1.{region}.{vault-id}.{key-id}
            // 例如: ocid1.key.oc1.eu-paris-1.h5usmibaaaays.abrwiljr7ue3kf6xyq57ffly64xxouko2ryy6l6halrawnazerrlmsd6l64a
            // Vault ID 是 h5usmibaaaays 部分
            String keyOcid = hsmProperties.getKeyOcid();
            if (keyOcid == null || keyOcid.trim().isEmpty()) {
                throw new IllegalArgumentException("Key OCID is required but not configured");
            }

            String[] keyOcidParts = keyOcid.split("\\.");
            if (keyOcidParts.length < 5) {
                throw new IllegalArgumentException("Invalid key OCID format: " + keyOcid);
            }

            // vault ID 是第 5 个部分（索引 4）
            String vaultId = keyOcidParts[4];
            String region = hsmProperties.getRegion();

            // 构建正确的 KMS crypto endpoint
            // 格式: https://{vault-id}-crypto.kms.{region}.oci.oraclecloud.com
            String endpoint = String.format("https://%s-crypto.kms.%s.oci.oraclecloud.com", vaultId, region);

            log.info("Initializing KMS Crypto Client with endpoint: {}", endpoint);

            // 使用系统属性禁用 Apache Connector（如果存在版本冲突）
            // 由于我们已经添加了正确版本的 Apache Connector 依赖，通常不需要禁用
            // 如果仍有问题，可以通过系统属性禁用：
            // System.setProperty("jersey.config.client.useApacheConnector", "false");

            kmsCryptoClient = KmsCryptoClient.builder()
                    .endpoint(endpoint)
                    .build(authenticationProvider);

            log.info("Oracle HSM service initialized successfully. Key OCID: {}, Vault ID: {}, Region: {}, Endpoint: {}",
                    hsmProperties.getKeyOcid(), vaultId, region, endpoint);
        } catch (Exception e) {
            log.error("Failed to initialize Oracle HSM service", e);
            throw new RuntimeException("Failed to initialize Oracle HSM service", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (kmsCryptoClient != null) {
            kmsCryptoClient.close();
        }
        log.info("Oracle HSM service closed");
    }

    @Override
    public String sign(byte[] data) throws Exception {
        if (!hsmProperties.isEnabled()) {
            throw new IllegalStateException("Oracle HSM is disabled");
        }

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data to sign cannot be null or empty");
        }

        try {
            // 对于 Sha512RsaPkcsPss 算法，当使用 MessageType.Digest 时，
            // Oracle KMS 期望接收的是 SHA-512 哈希值（64字节），而不是原始数据
            // 因此需要先对数据进行 SHA-512 哈希处理
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(data);
            String base64Hash = Base64.getEncoder().encodeToString(hashBytes);

            log.debug("Signing data using HSM key: {}, keyVersion: {}, original data length: {} bytes, hash length: {} bytes",
                    hsmProperties.getKeyOcid(),
                    hsmProperties.getKeyVersionOcid(),
                    data.length,
                    hashBytes.length);

            // 创建签名请求详情
            // loggingContext 是可选的，用于在日志中记录额外的元数据
            // 如果不需要日志追踪，可以设置为 null 或不设置
            // 如果需要追踪，可以添加有意义的键值对，如操作类型、请求ID等
            SignDataDetails.Builder signDataDetailsBuilder = SignDataDetails.builder()
                    .message(base64Hash)
                    .keyId(hsmProperties.getKeyOcid())
                    .messageType(SignDataDetails.MessageType.Digest)
                    .signingAlgorithm(SignDataDetails.SigningAlgorithm.Sha512RsaPkcsPss);

            // 只有当 keyVersionOcid 不为空时才设置，否则让 KMS 使用当前活动的密钥版本
            if (hsmProperties.getKeyVersionOcid() != null && !hsmProperties.getKeyVersionOcid().trim().isEmpty()) {
                signDataDetailsBuilder.keyVersionId(hsmProperties.getKeyVersionOcid());
            }

            // loggingContext 是可选的，用于日志追踪
            // 可以设置为 null，或者添加有意义的键值对用于追踪
            // 例如：put("operation", "sign"), put("requestId", UUID.randomUUID().toString())
            signDataDetailsBuilder.loggingContext(null); // 或者根据需要设置追踪信息

            SignDataDetails signDataDetails = signDataDetailsBuilder.build();

            // opcRequestId 是可选的，用于请求追踪和幂等性
            // 最佳实践是不设置，让 Oracle SDK 自动生成唯一ID
            // 如果必须设置，应该使用 UUID 生成唯一ID
            SignRequest signRequest = SignRequest.builder()
                    .signDataDetails(signDataDetails)
                    // .opcRequestId(UUID.randomUUID().toString()) // 可选：如果需要手动设置，使用 UUID
                    .build();

            // 使用已初始化的 kmsCryptoClient
            SignResponse response = kmsCryptoClient.sign(signRequest);
            String signature = response.getSignedData().getSignature();

            log.debug("Data signed successfully using HSM key: {}, signature length: {}",
                    hsmProperties.getKeyOcid(), signature != null ? signature.length() : 0);
            return signature;
        } catch (BmcException e) {
            log.error("Failed to sign data using HSM. Status code: {}, Service code: {}, Message: {}, OpcRequestId: {}",
                    e.getStatusCode(), e.getServiceCode(), e.getMessage(), e.getOpcRequestId(), e);
            throw new Exception("Failed to sign data using HSM: " + e.getMessage() +
                    (e.getServiceCode() != null ? " (Service code: " + e.getServiceCode() + ")" : ""), e);
        } catch (Exception e) {
            log.error("Failed to sign data using HSM", e);
            throw new Exception("Failed to sign data using HSM: " + e.getMessage(), e);
        }
    }

    @Override
    public String sign(String data) throws Exception {
        return sign(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean verify(byte[] data, String signature) throws Exception {
        if (!hsmProperties.isEnabled()) {
            throw new IllegalStateException("Oracle HSM is disabled");
        }

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data to verify cannot be null or empty");
        }

        if (signature == null || signature.trim().isEmpty()) {
            throw new IllegalArgumentException("Signature cannot be null or empty");
        }

        try {
            // 尝试解码 Base64 以验证格式是否正确
            try {
                Base64.getDecoder().decode(signature);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Base64 signature: cannot decode. Signature length: {}", signature.length(), e);
                throw new IllegalArgumentException("Invalid Base64 signature: cannot decode", e);
            }

            // 对于 Sha512RsaPkcsPss 算法，当使用 MessageType.Digest 时，
            // Oracle KMS 期望接收的是 SHA-512 哈希值（64字节），而不是原始数据
            // 因此需要先对数据进行 SHA-512 哈希处理
            // 注意：必须与 sign 方法使用相同的算法和哈希函数
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(data);
            String base64Hash = Base64.getEncoder().encodeToString(hashBytes);

            log.debug("Verifying signature. Signature length: {}, Hash length: {} bytes",
                    signature.length(), hashBytes.length);

            VerifyDataDetails verifyDataDetails = VerifyDataDetails.builder()
                    .keyId(hsmProperties.getKeyOcid())
                    .keyVersionId(hsmProperties.getKeyVersionOcid())
                    .message(base64Hash)
                    .messageType(VerifyDataDetails.MessageType.Digest)
                    .signature(signature)
                    .signingAlgorithm(VerifyDataDetails.SigningAlgorithm.Sha512RsaPkcsPss)
                    .build();

            VerifyRequest verifyRequest = VerifyRequest.builder()
                    .verifyDataDetails(verifyDataDetails)
                    .build();

            // 执行验证
            VerifyResponse verifyResponse = kmsCryptoClient.verify(verifyRequest);
            boolean isValid = verifyResponse.getVerifiedData().getIsSignatureValid();

            log.debug("Signature verification result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Failed to verify signature using HSM", e);
            throw new Exception("Failed to verify signature using HSM: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(String data, String signature) throws Exception {
        return verify(data.getBytes(StandardCharsets.UTF_8), signature);
    }

    @Override
    public String encrypt(byte[] plaintext) throws Exception {
        if (!hsmProperties.isEnabled()) {
            throw new IllegalStateException("Oracle HSM is disabled");
        }

        try {
            // 将明文编码为 Base64
            String base64Plaintext = Base64.getEncoder().encodeToString(plaintext);

            // 创建加密请求
            EncryptDataDetails encryptDataDetails = EncryptDataDetails.builder()
                    .keyId(hsmProperties.getKeyOcid())
                    .keyVersionId(hsmProperties.getKeyVersionOcid())
                    .plaintext(base64Plaintext)
                    .encryptionAlgorithm(EncryptDataDetails.EncryptionAlgorithm.RsaOaepSha256)
                    .build();

            EncryptRequest encryptRequest = EncryptRequest.builder()
                    .encryptDataDetails(encryptDataDetails)
                    .build();

            // 执行加密
            EncryptResponse encryptResponse = kmsCryptoClient.encrypt(encryptRequest);
            String ciphertext = encryptResponse.getEncryptedData().getCiphertext();

            log.debug("Data encrypted successfully using HSM key: {}", hsmProperties.getKeyOcid());
            return ciphertext;
        } catch (Exception e) {
            log.error("Failed to encrypt data using HSM", e);
            throw new Exception("Failed to encrypt data using HSM: " + e.getMessage(), e);
        }
    }

    @Override
    public String encrypt(String plaintext) throws Exception {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] decrypt(String ciphertext) throws Exception {
        if (!hsmProperties.isEnabled()) {
            throw new IllegalStateException("Oracle HSM is disabled");
        }

        try {
            // 创建解密请求
            DecryptDataDetails decryptDataDetails = DecryptDataDetails.builder()
                    .ciphertext(ciphertext)
                    .keyId(hsmProperties.getKeyOcid())
                    .keyVersionId(hsmProperties.getKeyVersionOcid())
                    .encryptionAlgorithm(DecryptDataDetails.EncryptionAlgorithm.RsaOaepSha256)
                    .build();

            DecryptRequest decryptRequest = DecryptRequest.builder()
                    .decryptDataDetails(decryptDataDetails)
                    .build();

            // 执行解密
            DecryptResponse decryptResponse = kmsCryptoClient.decrypt(decryptRequest);
            String decryptedBase64 = decryptResponse.getDecryptedData().getPlaintext();

            // 解码 Base64 得到原始数据
            byte[] decryptedBytes = Base64.getDecoder().decode(decryptedBase64);

            log.debug("Data decrypted successfully using HSM key: {}", hsmProperties.getKeyOcid());
            return decryptedBytes;
        } catch (Exception e) {
            log.error("Failed to decrypt data using HSM", e);
            throw new Exception("Failed to decrypt data using HSM: " + e.getMessage(), e);
        }
    }

    @Override
    public String decryptToString(String ciphertext) throws Exception {
        byte[] decryptedBytes = decrypt(ciphertext);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

