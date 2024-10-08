package org.lzq.partnermatchbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
public class TeamAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 755554503765232287L;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍照片
     */
    private String teamImage;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
