package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;

/**
 * 用户服务
 * @author flash
 */
public interface UserService {
    /**
     * 获取用户信息
     *
     * @param token Token
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(String token);
}
