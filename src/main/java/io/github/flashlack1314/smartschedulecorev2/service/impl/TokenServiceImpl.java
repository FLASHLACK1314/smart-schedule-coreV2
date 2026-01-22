package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.config.TokenProperties;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenProperties tokenProperties;

    @Override
    public String generateToken(String userUuid, UserType userType) {
        // 生成64位Token（两个32位UUID拼接）
        String token = UuidUtil.generateUuidNoDash() + UuidUtil.generateUuidNoDash();

        // 创建Token信息
        TokenInfoDTO tokenInfo = new TokenInfoDTO();
        tokenInfo.setUserUuid(userUuid);
        tokenInfo.setUserType(userType);
        tokenInfo.setLoginTime(LocalDateTime.now());
        tokenInfo.setExpireTime(LocalDateTime.now().plusSeconds(
                tokenProperties.getExpiration().getSeconds()
        ));

        // 存储到Redis
        String redisKey = tokenProperties.getRedisKeyPrefix() + token;
        redisTemplate.opsForValue().set(
                redisKey,
                tokenInfo
        );

        // 如果启用多设备登录，维护用户Token索引
        if (tokenProperties.getAllowMultipleDevices()) {
            String userTokensKey = getUserTokensKey(userUuid, userType);
            redisTemplate.opsForSet().add(userTokensKey, token);
        }

        log.info("生成Token成功 - 用户类型: {}, 用户UUID: {}, Token前8位: {}...",
                userType, userUuid, token.substring(0, 8));

        return token;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String redisKey = tokenProperties.getRedisKeyPrefix() + token;
            return redisTemplate.hasKey(redisKey);
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public TokenInfoDTO getTokenInfo(String token) {
        String redisKey = tokenProperties.getRedisKeyPrefix() + token;
        Object tokenObj = redisTemplate.opsForValue().get(redisKey);

        if (tokenObj == null) {
            throw new BusinessException("Token不存在或已过期", ErrorCode.NOT_EXIST);
        }

        if (!(tokenObj instanceof TokenInfoDTO tokenInfo)) {
            throw new BusinessException("Token解析失败", ErrorCode.OPERATION_ERROR);
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(tokenInfo.getExpireTime())) {
            deleteToken(token);
            throw new BusinessException("Token已过期", ErrorCode.NOT_EXIST);
        }

        return tokenInfo;
    }

    @Override
    public void refreshToken(String token) {
        String redisKey = tokenProperties.getRedisKeyPrefix() + token;
        Boolean exists = redisTemplate.hasKey(redisKey);

        if (!exists) {
            throw new BusinessException("Token不存在或已过期", ErrorCode.NOT_EXIST);
        }

        // 更新过期时间
        redisTemplate.expire(
                redisKey,
                tokenProperties.getExpiration()
        );

        log.info("刷新Token成功 - Token前8位: {}...", token.substring(0, 8));
    }

    @Override
    public void deleteToken(String token) {
        String redisKey = tokenProperties.getRedisKeyPrefix() + token;

        // 获取Token信息（用于清理索引）
        try {
            TokenInfoDTO tokenInfo = getTokenInfo(token);
            String userTokensKey = getUserTokensKey(
                    tokenInfo.getUserUuid(),
                    tokenInfo.getUserType()
            );
            redisTemplate.opsForSet().remove(userTokensKey, token);
        } catch (BusinessException e) {
            // Token已不存在，忽略
        }

        redisTemplate.delete(redisKey);
        log.info("删除Token成功 - Token前8位: {}...", token.substring(0, 8));
    }

    @Override
    public void deleteAllUserTokens(String userUuid, UserType userType) {
        String userTokensKey = getUserTokensKey(userUuid, userType);
        Set<Object> tokensObj = redisTemplate.opsForSet().members(userTokensKey);

        if (tokensObj != null) {
            tokensObj.forEach(token -> {
                String redisKey = tokenProperties.getRedisKeyPrefix() + token;
                redisTemplate.delete(redisKey);
            });
        }

        redisTemplate.delete(userTokensKey);
        log.info("删除用户所有Token成功 - 用户类型: {}, 用户UUID: {}", userType, userUuid);
    }

    /**
     * 获取用户Token索引的Redis Key
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     * @return Redis Key
     */
    private String getUserTokensKey(String userUuid, UserType userType) {
        return tokenProperties.getUserTokensPrefix() + userType.name() + ":" + userUuid;
    }
}