package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.GetUserLoginDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.ChangePasswordVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.LoginVO;
import io.github.flashlack1314.smartschedulecorev2.service.AuthService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 用户登录
     *
     * @param getData 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<GetUserLoginDTO>> userLogin(
            @Valid @RequestBody LoginVO getData) {
        log.info("用户登录请求: userType={}, userName={}", getData.getUserType(), getData.getUserName());
        GetUserLoginDTO result = authService.login(
                getData.getUserType(), getData.getUserName(), getData.getPassword());
        return ResultUtil.success("登录成功", result);
    }

    /**
     * 用户登出
     * @param token  Token
     * @return 登出结果
     */
    @DeleteMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> userLogout(
            @RequestHeader("Authorization") String token
    ){
        authService.logout(cleanBearerPrefix(token));
        return ResultUtil.success("登出成功");
    }

    /**
     * 修改密码
     * @param getData 修改密码信息
     * @param token Token
     * @return 修改密码结果
     */
    @PostMapping("/change-password")
    @RequireRole({UserType.ACADEMIC_ADMIN,UserType.TEACHER,UserType.STUDENT})
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordVO getData

    ){
        authService.changePassword(getData, cleanBearerPrefix(token));
        return ResultUtil.success("修改密码成功");
    }

    /**
     * 验证Token是否有效
     * <p>
     * ⚠️ 此接口只允许系统管理员和教务管理员访问
     * 用于 Dify 智能调课助手在工作流开始时验证用户身份
     *
     * @param authHeader Authorization Header（支持 Bearer 前缀）
     * @return 验证结果（Token信息）
     */
    @GetMapping("/verify")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<TokenInfoDTO>> verifyToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = this.cleanBearerPrefix(authHeader);
        log.info("Token验证请求");
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        return ResultUtil.success("Token验证成功", tokenInfo);
    }

    /**
     * 清理 Token 的 Bearer 前缀
     *
     * @param authHeader Authorization Header
     * @return 纯 Token 字符串
     */
    private String cleanBearerPrefix(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return authHeader;
    }
}
