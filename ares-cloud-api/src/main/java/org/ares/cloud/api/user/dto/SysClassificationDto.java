package org.ares.cloud.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SysClassificationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 2941613050292143709L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private String id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String classificationName;

    /**
     * 父类ID（层级关联，根节点可填固定值如0）
     */
    @Schema(description = "父类ID（层级关联，根节点可填固定值如0）")
    private String parentId;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    @Schema(description = "状态(1:正常,2:停用)，默认值1")
    private Integer status;

    private List<SysClassificationUrlDto> urlList;

    private List<SysClassificationDto> children;

}
