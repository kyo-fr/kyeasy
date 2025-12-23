package org.ares.cloud.user.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ares.cloud.common.query.Query;

/**
 * @author system
 * @description 操作日志 查询原型
 * @date 2024/12/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "操作日志查询")
public class SysOperationLogQuery extends Query {

    @Schema(description = "操作用户ID")
    @JsonProperty(value = "userId")
    private Long userId;

    @Schema(description = "操作角色ID")
    @JsonProperty(value = "roleId")
    private Long roleId;

    @Schema(description = "操作状态（1-成功，0-失败）")
    @JsonProperty(value = "status")
    private Integer status;

    @Schema(description = "请求URL")
    @JsonProperty(value = "url")
    private String url;

    @Schema(description = "请求方法")
    @JsonProperty(value = "requestMethod")
    private String requestMethod;
}

