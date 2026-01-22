package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import io.github.flashlack1314.smartschedulecorev2.model.dto.GetUserLoginDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {


    @PostMapping("/login")
    public ResponseEntity<BaseResponse<GetUserLoginDTO>> userLogin(
            @Valid @RequestBody LoginVO getData) {
        return null;
    }
}
