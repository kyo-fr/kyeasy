package org.ares.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("SYS_USER_ROLE")
public class SysUserRoleEntity implements Serializable {


    @Serial
    private static final long serialVersionUID = 4409826500976239256L;
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField("USER_ID")
    private String userId;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableField("ROLE_ID")
    private String roleId;

    /**
     * 国家代码
     */
    @Schema(description = "国家代码", example = "+33")
    @TableField(exist = false)
    private String countryCode;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    @TableField(exist = false)
    private String phone;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    @Schema(description = "状态(1:正常,2:停用)，默认值1")
    @TableField("STATUS")
    private Integer status;

    /**
     * 删除标记（0-未删除；1-已删除），默认值0
     */
    @Schema(description = "删除标记（0-未删除；1-已删除），默认值0")
    @TableField("DELETED")
    private Integer deleted;

    /**
     * 版本号（乐观锁字段）
     */
    @Schema(description = "版本号（乐观锁字段）")
    @TableField("VERSION")
    private Integer version;

    /**
     * 创建时间（13位时间戳，精确到毫秒）
     */
    @Schema(description = "创建时间（13位时间戳，精确到毫秒）")
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

    /**
     * 数据签名
     */
    @TableField("DATA_SIGNATURE")
    private String dataSignature;

    /**
     * 数据哈希
     */
    @TableField("DATA_HASH")
    private String dataHash;

    /**
     * 公钥ID
     */
    @TableField("PUBLIC_KEY_ID")
    private String publicKeyId;

    /**
     * 业务编号
     */
    @TableField("BUSINESS_NO")
    private Long businessNo;

    /**
     * 最后签名时间（13位时间戳，精确到毫秒）
     */
    @TableField("LAST_SIGN_TIME")
    private Long lastSignTime;

    /**
     * 签名算法
     */
    @TableField("SIGN_ALGORITHM")
    private String signAlgorithm;

}
