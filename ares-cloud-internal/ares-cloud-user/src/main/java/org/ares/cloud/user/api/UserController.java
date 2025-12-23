package org.ares.cloud.user.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.ares.cloud.api.user.dto.UserDto;
import org.ares.cloud.api.user.enums.UserIdentity;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.dto.PageResult;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.user.query.UserQuery;
import org.ares.cloud.user.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
* @author hugo tangxkwork@163.com
* @description 用户 控制器
* @version 1.0.0
* @date 2024-10-11
*/
@RestController
@RequestMapping("/api/user/v1/users")
@Tag(name="用户")
public class UserController {
    @Resource
    private UserService userService;


    @GetMapping("page")
    @Operation(summary = "分页")
   // @PreAuthorize("hasAuthority('user:ares-users:page')")
    public Result<PageResult<UserDto>> page(@ParameterObject @Valid UserQuery query){
        PageResult<UserDto> page = userService.loadList(query);

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
    @Operation(summary = "详情")
    //@PreAuthorize("hasAuthority('user:ares-users:info')")
    public Result<UserDto> get(@PathVariable("id") String id){
         UserDto dto= userService.loadById(id);
        Integer identity = ApplicationContext.getIdentity();
        System.out.println(identity);
        return Result.success(dto);
    }


    @PostMapping
    @Operation(summary = "保存")
   // @PreAuthorize("hasAuthority('user:ares-users:save')")
    public Result<String> save(@RequestBody @Valid UserDto dto){
        dto.setIdentity(UserIdentity.PlatformUsers.getValue());
        userService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改")
    //@PreAuthorize("hasAuthority('user:ares-users:update')")
    public Result<String> update(@RequestBody @Valid UserDto dto){
        userService.update(dto);
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
    @Operation(summary = "根据id删除")
    //@PreAuthorize("hasAuthority('user:ares-users:del_by_id')")
    public Result<String> del(@PathVariable("id") String id){
        userService.deleteById(id);
        return Result.success();
    }

}