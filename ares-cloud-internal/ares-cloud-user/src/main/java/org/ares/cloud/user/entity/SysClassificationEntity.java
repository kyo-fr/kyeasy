package org.ares.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("SYS_CLASSIFICATION")
public class SysClassificationEntity implements Serializable {


    @Serial
    private static final long serialVersionUID = -2976427253745985343L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 分类名称
     */
    @TableField("CLASSIFICATION_NAME")
    private String classificationName;

    /**
     * 父类ID（层级关联，根节点可填固定值如0）
     */
    @TableField("PARENT_ID")
    private String parentId;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 删除标记（0-未删除；1-已删除），默认值0
     */
    @TableField("DELETED")
    private Integer deleted;

    /**
     * 版本号（乐观锁字段）
     */
    @TableField("VERSION")
    private Integer version;

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
