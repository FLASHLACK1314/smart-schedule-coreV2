package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;

/**
 * 系统管理员服务接口
 *
 * @author flash
 */
public interface SystemAdminService {

    /**
     * 根据用户类型获取用户信息
     *
     * @param page     页码
     * @param size     每页数量
     * @param userType 用户类型
     * @param userName 用户名
     * @return 用户信息列表
     */
    PageDTO<UserInfoDTO> getUserInfoPageByType(
            int page,
            int size,
            String userType,
            String userName);

    /**
     * 更新密码
     *
     * @param userUuid    用户UUID
     * @param userType    用户类型
     * @param newPassword 新密码
     */
    void updatePassword(
            String userUuid,
            String userType,
            String newPassword
    );
}
