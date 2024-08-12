package org.lzq.partnermatchbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 *
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 用户昵称
     */
    private String username;
    /**
     * 密码
     */
    private String userpassword;



}
