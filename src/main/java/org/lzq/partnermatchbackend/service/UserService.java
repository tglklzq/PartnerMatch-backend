package org.lzq.partnermatchbackend.service;

import org.lzq.partnermatchbackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lzq.partnermatchbackend.model.request.UserLoginRequest;
import org.lzq.partnermatchbackend.model.request.UserRegisterRequest;
import org.lzq.partnermatchbackend.model.request.UserUpdateRequest;
import org.lzq.partnermatchbackend.model.response.UserLoginResponse;

/**
* @author liangzhiquan
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-08-12 02:11:46
*/
public interface UserService extends IService<User> {


    void userRegister(UserRegisterRequest userRegisterRequest);

    UserLoginResponse userLogin(UserLoginRequest requestParam);

    void updateUser(UserUpdateRequest requestParam);

    Boolean checkLogin(String userName, String token);

    void logout(String userName, String token);
}
