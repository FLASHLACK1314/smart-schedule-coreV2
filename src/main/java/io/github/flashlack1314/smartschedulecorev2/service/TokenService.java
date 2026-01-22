package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;

/**
 * Token服务接口
 *
 * @author flash
 */
public interface TokenService {

    /**
     * 生成Token并存储到Redis
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     * @return 生成的Token
     */
    String generateToken(String userUuid, UserType userType);

    /**
     * 验证Token是否有效
     *
     * @param token Token字符串
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取Token信息
     *
     * @param token Token字符串
     * @return Token信息
     */
    TokenInfoDTO getTokenInfo(String token);

    /**
     * 刷新Token过期时间
     *
     * @param token Token字符串
     */
    void refreshToken(String token);

    /**
     * 删除Token（退出登录）
     *
     * @param token Token字符串
     */
    void deleteToken(String token);

    /**
     * 删除用户的所有Token（强制登出）
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     */
    void deleteAllUserTokens(String userUuid, UserType userType);
}