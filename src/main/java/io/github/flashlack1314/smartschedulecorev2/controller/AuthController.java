package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.model.dto.GetUserLoginDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.LoginVO;
import io.github.flashlack1314.smartschedulecorev2.service.AuthService;
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

    /**
     * 用户登录
     *
     * @param getData 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<GetUserLoginDTO>> userLogin(
            @Valid @RequestBody LoginVO getData) {
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
        authService.logout(token);
        return ResultUtil.success("登出成功");
    }
}
