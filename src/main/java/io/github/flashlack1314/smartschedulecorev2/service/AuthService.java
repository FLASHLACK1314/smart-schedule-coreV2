package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.GetUserLoginDTO;

/**
 * 认证服务接口
 *
 * @author flash
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param userType 用户类型（STUDENT, TEACHER, ACADEMIC_ADMIN, SYSTEM_ADMIN）
     * @param userName 用户名/学号/工号
     * @param password 密码
     * @return 登录结果（包含Token和用户信息）
     */
    GetUserLoginDTO login(String userType, String userName, String password);

    /**
     * 用户退出登录
     *
     * @param token Token
     */
    void logout(String token);
}
