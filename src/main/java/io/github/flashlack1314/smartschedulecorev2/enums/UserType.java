package io.github.flashlack1314.smartschedulecorev2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author flash
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * 学生
     */
    STUDENT("学生"),

    /**
     * 教师
     */
    TEACHER("教师"),

    /**
     * 教务管理员
     */
    ACADEMIC_ADMIN("教务管理员"),

    /**
     * 系统管理员
     */
    SYSTEM_ADMIN("系统管理员");

    /**
     * 用户类型描述
     */
    private final String description;

    /**
     * 根据字符串获取用户类型
     *
     * @param value 用户类型字符串
     * @return 用户类型枚举
     * @throws IllegalArgumentException 如果传入的用户类型无效
     */
    public static UserType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户类型不能为空");
        }

        for (UserType type : UserType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的用户类型: " + value);
    }
}