package org.lzq.partnermatchbackend.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.lzq.partnermatchbackend.common.BaseResponse;
import org.lzq.partnermatchbackend.common.ErrorCode;
import org.lzq.partnermatchbackend.common.ResultUtils;
import org.lzq.partnermatchbackend.exception.BusinessException;
import org.lzq.partnermatchbackend.model.request.UserLoginRequest;
import org.lzq.partnermatchbackend.model.request.UserRegisterRequest;
import org.lzq.partnermatchbackend.model.request.UserUpdateRequest;
import org.lzq.partnermatchbackend.model.response.UserLoginResponse;
import org.lzq.partnermatchbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody @Valid UserRegisterRequest requestParam) {
        if (requestParam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userRegister(requestParam);
        return ResultUtils.success("注册成功");
        //return ResultUtils.success(null,"注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<UserLoginResponse> userLogin(@RequestBody @Valid UserLoginRequest requestParam) {
        if (requestParam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.userLogin(requestParam));
    }
    @PostMapping("/update")
    public BaseResponse<String> updateUser(@RequestBody @Valid UserUpdateRequest requestParam) {
        if (requestParam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updateUser(requestParam);
        return ResultUtils.success("更新成功");
    }
    @GetMapping("check-login")
    public BaseResponse<Boolean> checkLogin(@RequestParam ("username") String userName,@RequestParam("token") String token){
        boolean result =userService.checkLogin(userName,token);
        return  ResultUtils.success(result?"用户已登录":"用户未登录");
    }

}
