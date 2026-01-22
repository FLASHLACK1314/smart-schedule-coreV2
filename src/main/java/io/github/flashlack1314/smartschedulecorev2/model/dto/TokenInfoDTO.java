package io.github.flashlack1314.smartschedulecorev2.model.dto;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token信息DTO（Redis存储）
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoDTO {

    /**
     * 用户UUID
     */
    private String userUuid;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}