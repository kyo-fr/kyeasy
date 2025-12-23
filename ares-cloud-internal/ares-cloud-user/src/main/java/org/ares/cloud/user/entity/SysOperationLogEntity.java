package org.ares.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author system
 * @description 权限操作日志表，记录所有权限验证操作，满足NF525规范要求
 * @date 2024/12/19
 */
@Data
@TableName("SYS_OPERATION_LOG")
public class SysOperationLogEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5167327823691446778L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务连续编号（自增），用于NF525审计追踪，通过序列SEQ_SYS_OPERATION_LOG_BUSINESS_NO自动生成
     */
    @TableField("BUSINESS_NO")
    private Integer businessNo;

    /**
     * 版本号（乐观锁字段），防止并发修改，默认值0
     */
    @TableField("VERSION")
    private Integer version;

    /**
     * 操作用户ID
     */
    @TableField("USER_ID")
    private Long userId;

    /**
     * 操作角色ID
     */
    @TableField("ROLE_ID")
    private Long roleId;

    /**
     * 请求的URL地址
     */
    @TableField("URL")
    private String url;

    /**
     * HTTP请求方法（GET、POST、PUT、DELETE等）
     */
    @TableField("REQUEST_METHOD")
    private String requestMethod;

    /**
     * 客户端IP地址
     */
    @TableField("IP_ADDRESS")
    private String ipAddress;

    /**
     * 客户端User-Agent信息（可选）
     */
    @TableField("USER_AGENT")
    private String userAgent;

    /**
     * 操作状态（1-成功，0-失败）
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 错误信息（操作失败时记录）
     */
    @TableField("ERROR_MSG")
    private String errorMsg;

    /**
     * 响应时间（毫秒），用于性能分析
     */
    @TableField("RESPONSE_TIME")
    private Long responseTime;

    /**
     * 操作HSM签名，用于验证本次权限操作的有效性，签名内容包含：USER_ID + ROLE_ID + URL + REQUEST_METHOD + 时间戳
     */
    @TableField("SIGNATURE")
    private String signature;

    /**
     * 数据完整性HSM签名，用于确保日志记录本身不可篡改，满足NF525不可篡改性要求，签名内容包含：ID + 关键业务字段 + 时间戳
     */
    @TableField("DATA_SIGNATURE")
    private String dataSignature;

    /**
     * 数据哈希值（SHA-256），用于快速检测数据完整性，哈希内容包含：ID + 关键业务字段 + VERSION + STATUS + DELETED
     */
    @TableField("DATA_HASH")
    private String dataHash;

    /**
     * HSM公钥标识（密钥OCID或版本OCID），用于签名验证，支持密钥轮换场景
     */
    @TableField("PUBLIC_KEY_ID")
    private String publicKeyId;

    /**
     * 签名算法，如：SHA256RSA_PKCS1_V15，默认值SHA256RSA_PKCS1_V15
     */
    @TableField("SIGN_ALGORITHM")
    private String signAlgorithm;

    /**
     * 最后签名时间（13位时间戳，精确到毫秒）
     */
    @TableField("LAST_SIGN_TIME")
    private Long lastSignTime;

    /**
     * 请求追踪ID，便于分布式系统追踪和问题排查
     */
    @TableField("TRACE_ID")
    private String traceId;

    /**
     * 请求体数据（可选），建议加密存储，避免敏感信息泄露
     */
    @TableField("REQUEST_BODY")
    private String requestBody;

    /**
     * 响应体数据（可选），建议加密存储，避免敏感信息泄露
     */
    @TableField("RESPONSE_BODY")
    private String responseBody;

    /**
     * 删除标记（0-未删除，1-已删除），默认值0
     */
    @TableField("DELETED")
    private Integer deleted;

    /**
     * 创建时间（13位时间戳，精确到毫秒）
     */
    @TableField("CREATE_TIME")
    private Long createTime;

    /**
     * 创建人
     */
    @TableField("CREATOR")
    private String creator;

    /**
     * 更新时间（13位时间戳，精确到毫秒）
     */
    @TableField("UPDATE_TIME")
    private Long updateTime;

    /**
     * 更新人
     */
    @TableField("UPDATER")
    private String updater;
}
