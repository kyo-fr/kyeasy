package org.ares.cloud.platformInfo.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ares.cloud.api.user.annotation.RequireUrlPermission;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.platformInfo.dto.PlatformApprovalDto;
import org.ares.cloud.platformInfo.dto.PlatformApprovalSettlementDto;
import org.ares.cloud.platformInfo.query.PlatformApprovalQuery;
import org.ares.cloud.platformInfo.service.PlatformApprovalService;
import org.ares.cloud.platformInfo.vo.PlatformApprovalRecordVo;
import org.ares.cloud.platformInfo.vo.PlatformApprovalVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author hugo tangxkwork@163.com
* @description 平台审批 控制器
* @version 1.0.0
* @date 2024-10-31
*/
@RestController
@RequestMapping("/api/platform/v1/approval")
@Tag(name="平台审批")
@AllArgsConstructor
public class PlatformApprovalController {
    @Resource
    private PlatformApprovalService platformApprovalService;


    @GetMapping("page")
    @Operation(summary = "分页(审批列表)")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformApproval:page')")
    public Result<PageResult<PlatformApprovalVo>> page(@ParameterObject @Valid PlatformApprovalQuery query){
        PageResult<PlatformApprovalVo> page = platformApprovalService.loadList(query);
        return Result.success(page);
    }

    /**
     * 收入列表
     * @param query
     * @return
     */
    @GetMapping("page/income")
    @Operation(summary = "分页(收入列表)")
    @RequireUrlPermission
    public Result<PageResult<PlatformApprovalVo>> income(@ParameterObject @Valid PlatformApprovalQuery query){
        PageResult<PlatformApprovalVo> page = platformApprovalService.income(query);
        return Result.success(page);
    }

    @Operation(summary = "获取所有")
    @RequireUrlPermission
//    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('platformInfo:PlatformApproval:all')")
    public  Result<List<PlatformApprovalDto>> all(){
        List<PlatformApprovalDto> all = platformApprovalService.loadAll();
        return Result.success(all);
    }

    @Parameter(
        name = "id",
        description = "主键",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "string")
    )
    @GetMapping("{id}")
    @Operation(summary = "详情")
    @RequireUrlPermission
    //@PreAuthorize("hasAuthority('platformInfo:PlatformApproval:info')")
    public Result<PlatformApprovalVo> get(@PathVariable("id") String id){
        PlatformApprovalVo dto= platformApprovalService.loadById(id);
         return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "审批保存")
    @RequireUrlPermission
   // @PreAuthorize("hasAuthority('platformInfo:PlatformApproval:save')")
    public Result<PlatformApprovalDto> save(@RequestBody @Valid PlatformApprovalDto dto){
        try {
            return Result.success(platformApprovalService.create(dto));
        }catch (Exception e){
            throw new RpcCallException(e);
        }    }


    @GetMapping("/getApprovalByUserId")
    @Operation(summary = "根据用户查询审批单-ipad端使用",description = "ipad端使用")
    @RequireUrlPermission
    public Result<PlatformApprovalDto> getApprovalByUserId(@RequestParam("userId") String userId) {
        PlatformApprovalDto dto= platformApprovalService.getApprovalByUserId(userId);
        return Result.success(dto);
    }


    @PostMapping("/manualSettlementOrder")
    @Operation(summary = "审批单核算")
    @RequireUrlPermission
    public Result<String> manualSettlementOrder(@RequestBody @Valid PlatformApprovalSettlementDto platformApprovalSettlementDto){
//        try {
            platformApprovalService.manualSettlementOrder(platformApprovalSettlementDto);
            return Result.success();
//        } catch (Exception e) {
//            throw new RpcCallException(e);
//        }
    }


    @PostMapping("/counts")
    @Operation(summary = "管理端首页存储使用统计")
    @RequireUrlPermission
    public Result<PlatformApprovalRecordVo> counts(){
        PlatformApprovalRecordVo dto=   platformApprovalService.counts();
        return Result.success(dto);
    }

}