package org.lzq.partnermatchbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户登录请求体
 */
@Data
public class UserTagAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6121458871274540023L;
    /**
     * 标签
     */
    private String tag;
    /**
     * 用户ID
     */
    private Long userId;

}
