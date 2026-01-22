package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理员用户信息DTO
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemAdminUserInfoDTO {

    /**
     * 管理员UUID
     */
    private String adminUuid;

    /**
     * 管理员用户名
     */
    private String adminUsername;
}