package org.lzq.partnermatchbackend.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 用户敏感信息脱敏工具类
 */
public class DesensitizationUtils {
    /**
     * 脱敏用户名，只显示前两位，其他用 * 代替
     */
    public static String desensitizeUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return username.charAt(0) + "*" + username.charAt(username.length() - 1);
    }

    /**
     * 脱敏手机号码，只显示前三位和后四位，中间部分用 * 代替
     */
    public static String desensitizePhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return null;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 脱敏邮箱地址，只显示前两位和 @ 之前的后两位，中间部分用 * 代替
     */
    public static String desensitizeEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        int index = email.indexOf("@");
        if (index <= 2) {
            return email;
        }
        return email.substring(0, 2) + "****" + email.substring(index - 2);
    }

    /**
     * 脱敏IP地址，隐藏后两段
     */
    public static String desensitizeIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return null;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 4) {
            return ip;
        }
        return parts[0] + "." + parts[1] + ".***.***";
    }
}
