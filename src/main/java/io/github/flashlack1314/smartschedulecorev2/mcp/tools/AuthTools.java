package io.github.flashlack1314.smartschedulecorev2.mcp.tools;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * MCP 认证工具类
 * 提供 Token 验证功能，供 Dify 工作流在开始时调用
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTools {

    private final TokenService tokenService;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 允许访问的用户类型
     */
    private static final List<UserType> ALLOWED_USER_TYPES = Arrays.asList(
            UserType.SYSTEM_ADMIN,
            UserType.ACADEMIC_ADMIN
    );

    /**
     * 验证 Token 是否有效
     * 用于 Dify 工作流开始时验证用户身份
     *
     * @param token 认证 Token（支持 Bearer 前缀）
     * @return 验证结果信息
     */
    @Tool(description = "验证用户Token是否有效。用于Dify工作流开始时的身份认证。" +
            "只有系统管理员(SYSTEM_ADMIN)和教务管理员(ACADEMIC_ADMIN)可以通过验证。")
    public String verifyToken(
            @ToolParam(description = "认证Token，从前端登录后获取") String token) {

        try {
            // 1. 检查 Token 是否为空
            if (token == null || token.trim().isEmpty()) {
                return "【认证失败】缺少认证Token，请先登录。";
            }

            // 2. 清理 Bearer 前缀
            String cleanToken = cleanBearerPrefix(token);

            // 3. 获取 Token 信息（会自动验证 Token 有效性）
            TokenInfoDTO tokenInfo = tokenService.getTokenInfo(cleanToken);

            // 4. 检查用户类型
            if (!ALLOWED_USER_TYPES.contains(tokenInfo.getUserType())) {
                log.warn("MCP认证失败 - 权限不足，用户类型: {}", tokenInfo.getUserType());
                return "【认证失败】权限不足，只有系统管理员和教务管理员可以使用智能调课助手。";
            }

            // 5. 认证成功
            log.info("MCP认证成功 - 用户类型: {}, UUID: {}",
                    tokenInfo.getUserType(), tokenInfo.getUserUuid());

            return String.format("【认证成功】\n用户类型: %s\n用户UUID: %s\n登录时间: %s\n过期时间: %s",
                    tokenInfo.getUserType(),
                    tokenInfo.getUserUuid(),
                    tokenInfo.getLoginTime(),
                    tokenInfo.getExpireTime());

        } catch (Exception e) {
            log.warn("MCP认证失败 - {}", e.getMessage());
            return "【认证失败】" + e.getMessage();
        }
    }

    /**
     * 清理 Token 的 Bearer 前缀
     */
    private String cleanBearerPrefix(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }
}
