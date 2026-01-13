package org.ares.cloud.service;

import java.nio.charset.StandardCharsets;

/**
 * Oracle HSM 服务接口
 *
 * @author system
 * @version 1.0
 * @description: Oracle HSM/KMS 服务接口
 */
public interface OracleHsmService {

    /**
     * 使用 HSM 密钥对数据进行签名
     *
     * @param data 待签名的数据
     * @return Base64 编码的签名
     * @throws Exception 签名失败时抛出异常
     */
    String sign(byte[] data) throws Exception;

    /**
     * 使用 HSM 密钥对字符串进行签名
     *
     * @param data 待签名的字符串
     * @return Base64 编码的签名
     * @throws Exception 签名失败时抛出异常
     */
    String sign(String data) throws Exception;

    /**
     * 验证签名
     *
     * @param data 原始数据
     * @param signature Base64 编码的签名
     * @return 验证结果
     * @throws Exception 验证失败时抛出异常
     */
    boolean verify(byte[] data, String signature) throws Exception;

    /**
     * 验证签名
     *
     * @param data 原始字符串
     * @param signature Base64 编码的签名
     * @return 验证结果
     * @throws Exception 验证失败时抛出异常
     */
    boolean verify(String data, String signature) throws Exception;

    /**
     * 使用 HSM 密钥加密数据
     *
     * @param plaintext 明文数据
     * @return Base64 编码的密文
     * @throws Exception 加密失败时抛出异常
     */
    String encrypt(byte[] plaintext) throws Exception;

    /**
     * 使用 HSM 密钥加密字符串
     *
     * @param plaintext 明文字符串
     * @return Base64 编码的密文
     * @throws Exception 加密失败时抛出异常
     */
    String encrypt(String plaintext) throws Exception;

    /**
     * 使用 HSM 密钥解密数据
     *
     * @param ciphertext Base64 编码的密文
     * @return 解密后的明文数据
     * @throws Exception 解密失败时抛出异常
     */
    byte[] decrypt(String ciphertext) throws Exception;

    /**
     * 使用 HSM 密钥解密字符串
     *
     * @param ciphertext Base64 编码的密文
     * @return 解密后的明文字符串
     * @throws Exception 解密失败时抛出异常
     */
    String decryptToString(String ciphertext) throws Exception;
}

