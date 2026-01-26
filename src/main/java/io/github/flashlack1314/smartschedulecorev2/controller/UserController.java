package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.AuthService;
import io.github.flashlack1314.smartschedulecorev2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    /**
     * 获取用户信息
     *
     * @param token 用户Token
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public ResponseEntity<BaseResponse<UserInfoDTO>> getUserInfo(
            @RequestHeader("Authorization") String token
    ) {
        UserInfoDTO result = userService.getUserInfo(token);
        return ResultUtil.success("获取用户信息成功", result);
    }
}
