package org.ares.cloud.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.ares.cloud.service.OracleHsmService;
import org.springframework.stereotype.Component;

/**
 * Oracle HSM 使用示例
 *
 * @author system
 * @version 1.0
 * @description: 演示如何使用 Oracle HSM 服务进行签名、验证、加密、解密操作
 */
@Slf4j
@Component
public class HsmUsageExample {

    @Resource
    private OracleHsmService oracleHsmService;

    /**
     * 示例：使用 HSM 进行数据签名和验证
     */
    public void signAndVerifyExample() {
        try {
            String data = "Hello, Oracle HSM!";

            // 1. 使用 HSM 对数据进行签名
            String signature = oracleHsmService.sign(data);
            log.info("签名结果: {}", signature);

            // 2. 验证签名
            boolean isValid = oracleHsmService.verify(data, signature);
            log.info("签名验证结果: {}", isValid);

        } catch (Exception e) {
            log.error("签名或验证失败", e);
        }
    }

    /**
     * 示例：使用 HSM 进行数据加密和解密
     */
    public void encryptAndDecryptExample() {
        try {
            String plaintext = "敏感数据需要加密";

            // 1. 使用 HSM 加密数据
            String ciphertext = oracleHsmService.encrypt(plaintext);
            log.info("加密后的密文: {}", ciphertext);

            // 2. 使用 HSM 解密数据
            String decryptedText = oracleHsmService.decryptToString(ciphertext);
            log.info("解密后的明文: {}", decryptedText);

            // 验证加密解密是否成功
            if (plaintext.equals(decryptedText)) {
                log.info("加密解密成功！");
            } else {
                log.error("加密解密失败，数据不匹配");
            }

        } catch (Exception e) {
            log.error("加密或解密失败", e);
        }
    }

    /**
     * 示例：使用 HSM 对字节数组进行操作
     */
    public void byteArrayExample() {
        try {
            byte[] data = "二进制数据".getBytes();

            // 1. 签名
            String signature = oracleHsmService.sign(data);
            log.info("字节数组签名: {}", signature);

            // 2. 验证
            boolean isValid = oracleHsmService.verify(data, signature);
            log.info("字节数组签名验证: {}", isValid);

            // 3. 加密
            String ciphertext = oracleHsmService.encrypt(data);
            log.info("字节数组加密: {}", ciphertext);

            // 4. 解密
            byte[] decrypted = oracleHsmService.decrypt(ciphertext);
            log.info("字节数组解密长度: {}", decrypted.length);

        } catch (Exception e) {
            log.error("字节数组操作失败", e);
        }
    }
}

