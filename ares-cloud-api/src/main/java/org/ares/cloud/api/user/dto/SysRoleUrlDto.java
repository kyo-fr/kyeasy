package org.ares.cloud.api.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SysRoleUrlDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -276789501719509880L;
    /**
     * 主键ID
     */
    private String id;

    /**
     * 接口URL表主键ID（关联分类-URL表）
     */
    private String urlId;

    /**
     * 角色表主键ID（关联角色表）
     */
    private String roleId;

    /**
     * 状态(1:正常,2:停用)，默认值1
     */
    private Integer status;

    /**
     * 角色名称
     */
    private Long roleName;

    private List<RoleDto> roleDtoList;

    @Data
    public static class RoleDto {
        /**
         * 角色ID
         */
        private String roleId;
        /**
         * 角色名称
         */
        private String roleName;
    }

}
