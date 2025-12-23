package org.ares.cloud.api.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SysClassificationUrlDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 3874431145089657000L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 分类ID（关联分类表主键）
     */
    private String classificationId;

    /**
     * 接口URL
     */
    private String url;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    private Integer status;

}
