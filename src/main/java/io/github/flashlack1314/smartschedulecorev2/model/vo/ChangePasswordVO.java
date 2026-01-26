package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 修改密码的VO
 * @author flash
 */
@Getter
@Accessors(chain = true)
public class ChangePasswordVO {
    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 确认密码
     */
    private String confirmPassword;
}
