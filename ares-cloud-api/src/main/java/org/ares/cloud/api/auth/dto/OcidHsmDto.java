package org.ares.cloud.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OcidHsmDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 635539260639792282L;

    /**
     * 要签名的数据
     */
    @NotBlank(message = "要签名的数据不得为空", groups = {SignGroup.class, Verify.class})
    private String data;

    /**
     * Hsm返回签名的值
     */
    @NotBlank(message = "Hsm返回的签名不得为空", groups = {Verify.class})
    private String signature;

    /**
     * 要加密的数据
     */
    @NotBlank(message = "要加密的数据不得为空", groups = {Encrypt.class})
    private String plaintext;

    /**
     * hsm加密后的数据
     */
    @NotBlank(message = "要解密的数据不得为空", groups = {Decrypt.class})
    private String ciphertext;

    public interface SignGroup {}

    public interface Verify {}

    public interface Encrypt {}

    public interface Decrypt {}

}
