package org.ares.cloud.platformInfo.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.springdoc.core.annotations.ParameterObject;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.ares.cloud.platformInfo.convert.PlatformApprovalRecordConvert;
import org.ares.cloud.platformInfo.entity.PlatformApprovalRecordEntity;
import org.ares.cloud.platformInfo.service.PlatformApprovalRecordService;
import org.ares.cloud.platformInfo.query.PlatformApprovalRecordQuery;
import org.ares.cloud.platformInfo.dto.PlatformApprovalRecordDto;

/**
* @author hugo tangxkwork@163.com
* @description 存储变更明细表 控制器
* @version 1.0.0
* @date 2025-06-16
*/
@RestController
@RequestMapping("/api/platform/v1/platformApprovalRecord")
@Tag(name="商户存储变更")
@AllArgsConstructor
public class PlatformApprovalRecordController {
    @Resource
    private PlatformApprovalRecordService platformApprovalRecordService;


    @GetMapping("page")
    @Operation(summary = "分页",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:page')")
    public Result<PageResult<PlatformApprovalRecordDto>> page(@ParameterObject @Valid PlatformApprovalRecordQuery query){
        PageResult<PlatformApprovalRecordDto> page = platformApprovalRecordService.loadList(query);

        return Result.success(page);
    }


    @Parameter(
        name = "id",
        description = "主键",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @GetMapping("{id}")
    @Operation(summary = "详情",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:info')")
    public Result<PlatformApprovalRecordDto> get(@PathVariable("id") String id){
         PlatformApprovalRecordDto dto= platformApprovalRecordService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存",hidden = true)
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:save')")
    public Result<String> save(@RequestBody @Valid PlatformApprovalRecordDto dto){
        platformApprovalRecordService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:update')")
    public Result<String> update(@RequestBody @Valid PlatformApprovalRecordDto dto){
        platformApprovalRecordService.update(dto);
        return Result.success();
    }

    @Parameter(
    name = "id",
    description = "主键",
    required = true,
    in = ParameterIn.PATH,
    schema = @Schema(type = "string")
    )
    @DeleteMapping("{id}")
    @Operation(summary = "根据id删除",hidden = true)
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        platformApprovalRecordService.deleteById(id);
        return Result.success();
    }



    @PostMapping("/updatePlatformApprovalRecord")
    @Operation(summary = "商户存储消耗变更")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:platformApprovalRecord:update')")
    public Result<String> updatePlatformApprovalRecord(@RequestBody @Valid PlatformApprovalRecordDto dto){
        platformApprovalRecordService.updatePlatformApprovalRecord(dto);
        return Result.success();
    }


    @GetMapping("/getAvailableStorage")
    @Operation(summary = "获取商户存储")
    @RequireUrlPermission
    public Result<Long> getAvailableStorage(@RequestParam @Valid String tenantId){
        Long availableStorage = platformApprovalRecordService.getAvailableStorage(tenantId);
        return Result.success(availableStorage);
    }

}