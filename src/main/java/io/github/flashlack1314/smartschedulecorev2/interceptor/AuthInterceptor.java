package io.github.flashlack1314.smartschedulecorev2.interceptor;

import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 认证拦截器
 * 基于Token和角色的权限验证
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理HandlerMethod
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 检查是否需要认证（方法级别优先）
        RequireRole requireRole = getRequireRoleAnnotation(handlerMethod);

        // 如果没有注解，默认公开访问
        if (requireRole == null) {
            return true;
        }

        // 2. 提取Token
        String token = extractToken(request);
        if (token == null) {
            throw new com.xlf.utility.exception.BusinessException("未提供认证Token", com.xlf.utility.ErrorCode.UNAUTHORIZED);
        }

        // 3. 验证Token并获取用户信息
        TokenInfoDTO tokenInfo;
        try {
            tokenInfo = tokenService.getTokenInfo(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new com.xlf.utility.exception.BusinessException("Token无效或已过期", com.xlf.utility.ErrorCode.UNAUTHORIZED);
        }

        // 4. 检查角色权限
        UserType[] allowedRoles = requireRole.value();
        if (!Arrays.asList(allowedRoles).contains(tokenInfo.getUserType())) {
            log.warn("权限不足 - 用户类型: {}, 需要的角色: {}",
                    tokenInfo.getUserType(), Arrays.toString(allowedRoles));
            throw new com.xlf.utility.exception.BusinessException("权限不足", com.xlf.utility.ErrorCode.FORBIDDEN);
        }

        // 5. 将用户信息注入请求属性
        request.setAttribute("userUuid", tokenInfo.getUserUuid());
        request.setAttribute("userType", tokenInfo.getUserType());
        request.setAttribute("token", token);

        log.debug("用户认证成功 - UUID: {}, 类型: {}", tokenInfo.getUserUuid(), tokenInfo.getUserType());

        return true;
    }

    /**
     * 获取RequireRole注解
     * 方法级别优先于类级别
     *
     * @param handlerMethod 处理器方法
     * @return RequireRole注解，如果不存在返回null
     */
    @Nullable
    private RequireRole getRequireRoleAnnotation(HandlerMethod handlerMethod) {
        // 先检查方法级别
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 再检查类级别
        Class<?> beanType = handlerMethod.getBeanType();
        return beanType.getAnnotation(RequireRole.class);
    }

    /**
     * 从请求中提取Token
     * 支持 Authorization: Bearer {token} 格式
     *
     * @param request HttpServletRequest
     * @return Token字符串，如果不存在返回null
     */
    @Nullable
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
