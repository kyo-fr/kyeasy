package org.ares.cloud.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "主键ID")
    private String id;

    /**
     * 分类ID（关联分类表主键）
     */
    @Schema(description = "分类ID（关联分类表主键）")
    private String classificationId;

    /**
     * 接口URL
     */
    @Schema(description = "接口URL")
    private String url;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    @Schema(description = "状态(1:正常,2:停用)，默认值1")
    private Integer status;

}
