package io.github.flashlack1314.smartschedulecorev2.utils;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 * 用于从请求中获取当前用户信息
 *
 * @author flash
 */
public class SecurityUtils {

    private static final String USER_UUID_ATTR = "userUuid";
    private static final String USER_TYPE_ATTR = "userType";
    private static final String TOKEN_ATTR = "token";

    /**
     * 获取当前用户UUID
     *
     * @param request HttpServletRequest
     * @return 用户UUID
     * @throws IllegalStateException 如果用户信息不存在
     */
    public static String getCurrentUserUuid(HttpServletRequest request) {
        Object userUuid = request.getAttribute(USER_UUID_ATTR);
        if (userUuid == null) {
            throw new IllegalStateException("未找到用户信息，请确保请求已通过认证拦截器");
        }
        return (String) userUuid;
    }

    /**
     * 获取当前用户类型
     *
     * @param request HttpServletRequest
     * @return 用户类型
     * @throws IllegalStateException 如果用户类型不存在
     */
    public static UserType getCurrentUserType(HttpServletRequest request) {
        Object userType = request.getAttribute(USER_TYPE_ATTR);
        if (userType == null) {
            throw new IllegalStateException("未找到用户类型，请确保请求已通过认证拦截器");
        }
        return (UserType) userType;
    }

    /**
     * 获取当前Token
     *
     * @param request HttpServletRequest
     * @return Token字符串
     * @throws IllegalStateException 如果Token不存在
     */
    public static String getCurrentToken(HttpServletRequest request) {
        Object token = request.getAttribute(TOKEN_ATTR);
        if (token == null) {
            throw new IllegalStateException("未找到Token，请确保请求已通过认证拦截器");
        }
        return (String) token;
    }

    /**
     * 检查当前用户是否为指定类型
     *
     * @param request HttpServletRequest
     * @param userType 用户类型
     * @return 如果是指定类型返回true，否则返回false
     */
    public static boolean isUserType(HttpServletRequest request, UserType userType) {
        try {
            return getCurrentUserType(request) == userType;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
