package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.SystemAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 系统管理员控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/SystemAdmic")
public class SystemAdminController {
    private final SystemAdminService systemAdminService;

    /**
     * 根据用户类型获取用户信息
     *
     * @param page     页码
     * @param size     每页数量
     * @param userType 用户类型（可选，默认为STUDENT）
     * @param userName 用户名（可选，用于模糊查询）
     * @return 用户信息列表
     */
    @GetMapping("/getAcademicAdmin")
    @RequireRole({UserType.SYSTEM_ADMIN})
    public ResponseEntity<BaseResponse<PageDTO<UserInfoDTO>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "user_type", defaultValue = "STUDENT") String userType,
            @RequestParam(value = "user_name", required = false, defaultValue = "") String userName
    ) {
        log.info("接收到的参数 - page: {}, size: {}, userType: {}, userName: {}", page, size, userType, userName);
        PageDTO<UserInfoDTO> result =
                systemAdminService.getUserInfoPageByType(page, size, userType, userName);
        return ResultUtil.success("获取用户信息成功", result);
    }

    /**
     * 修改密码
     *
     * @param userUuid    用户uuid
     * @param userType    用户类型
     * @param newPassword 新密码
     * @return  修改密码结果
     */
    @PutMapping("/update-password")
    @RequireRole({UserType.SYSTEM_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestParam("user_uuid") String userUuid,
            @RequestParam("user_type") String userType,
            @RequestParam("new_password") String newPassword
    ) {
        systemAdminService.updatePassword(userUuid, userType, newPassword);
        return ResultUtil.success("修改密码成功");
    }


}
