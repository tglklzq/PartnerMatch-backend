package org.lzq.partnermatchbackend.utils;

import org.lzq.partnermatchbackend.common.BaseResponse;
import org.lzq.partnermatchbackend.common.ErrorCode;

/**
 * 返回工具类
 *
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }
    public static <T> BaseResponse<T> success(T data,String message){return  new BaseResponse<>(0,null,message);}

    public static <T> BaseResponse<T> success(String message) {
        return new BaseResponse<>(0, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
        public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }



    /**
     * 失败
     *
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse<Void> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), description);
    }
}
