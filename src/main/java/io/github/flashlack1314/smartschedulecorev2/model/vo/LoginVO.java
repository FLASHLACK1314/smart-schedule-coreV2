package io.github.flashlack1314.smartschedulecorev2.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 登录缓存
 * @author flash
 */
@Getter
@Accessors(chain = true)
public class LoginVO {
    /**
     * 用户类型（STUDENT, TEACHER, ACADEMIC_ADMIN, SYSTEM_ADMIN）
     */
    @JsonProperty("user_type")
    private String userType;

    /**
     * 用户名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 登录密码
     */
    private String password;
}
