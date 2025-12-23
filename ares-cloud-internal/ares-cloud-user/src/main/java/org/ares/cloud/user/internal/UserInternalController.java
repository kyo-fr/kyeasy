package org.ares.cloud.user.internal;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Resource;
import org.ares.cloud.api.user.dto.ChangePasswordReq;
import org.ares.cloud.api.user.dto.RecoverPasswordRequest;
import org.ares.cloud.api.user.dto.UserDto;
import org.ares.cloud.exception.RpcCallException;
import org.ares.cloud.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author hugo
 * @version 1.0
 * @description: TODO
 * @date 2024/10/7 23:19
 */
@RestController
@RequestMapping("/internal/user/v1/users")
public class UserInternalController {
    @Resource
    private UserService userService;

    /**
     * 根据账号获取用户
     * @param account 账号
     * @return 用户
     */
    @Hidden
    @GetMapping("get_by_account")
    public UserDto loadByAccount(@RequestParam("account") String account){
        UserDto dto= userService.loadByAccount(account);
        return dto;
    }
    /**
     * 加载和校验账号密码
     * @param account 长啊后
     * @param password 密码
     * @return 用户
     */
    @Hidden
    @GetMapping(value = "get_and_chick_account", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto loadAndChickPassword(@RequestParam("account") String account,@RequestParam("password") String password){
        return userService.loadAndChickPassword(account,password);
    }
    /**
     * 新增用户
     * @param dto 用户
     * @return id
     */
    @Hidden
    @PostMapping
    public String save(@RequestBody UserDto dto){
        userService.create(dto);
        return "";
    }

    /**
     * 获取用户详情
     * @param id 用户id
     * @return 用户详情
     */
    @Hidden
    @GetMapping("{id}")
    public UserDto get(@PathVariable("id") String id){
        return userService.loadById(id);
    }

    /**
     * 修改用户识别标志
     * @param id 用户id
     * @param identity 标识
     *
     */
    @Hidden
    @GetMapping("updateUserById")
    public UserDto updateUserById(@RequestParam("id") String id,@RequestParam("identity") Integer identity) {
        UserDto dto = userService.loadById(id);
        if (dto == null) {
            return null;
        }
        dto.setIdentity(identity);
        userService.update(dto);
        return dto;
    }

    /**
     * 修改用户密码
     * @param request 请求数据
     * @return 结果
     */
    @Hidden
    @PutMapping("change_password")
    public String changePassword(@RequestBody ChangePasswordReq request) {
        userService.changePassword(request);
       return "";
    }
    /**
     * 重置用户密码
     * @param request 请求数据
     * @return 结果
     */
    @Hidden
    @PutMapping("recover_password")
    public String recoverPassword(@RequestBody RecoverPasswordRequest request) {
        userService.recoverPassword(request);
        return "";
    }
    
    /**
     * 查询不存在则创建临时用户
     * @param phone 手机号
     * @param countryCode 国家编号
     * @return 用户ID
     */
    @Hidden
    @GetMapping("temporary")
    public UserDto getOrCreateTemporaryUser(@RequestParam("countryCode") String countryCode,@RequestParam("phone") String phone) {
        return userService.getOrCreateTemporaryUser(countryCode,phone);
    }
    
    /**
     * 根据账号查询不区分临时账号
     * @param account 账号
     * @return 用户列表
     */
    @Hidden
    @GetMapping("get_all_by_account")
    public UserDto loadAllByAccount(@RequestParam("account") String account) {
        return userService.loadAllByAccount(account);
    }
}
