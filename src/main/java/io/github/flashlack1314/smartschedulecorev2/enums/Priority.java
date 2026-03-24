package io.github.flashlack1314.smartschedulecorev2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公告优先级枚举
 *
 * @author flash
 */
@Getter
@AllArgsConstructor
public enum Priority {

    /**
     * 高优先级（红色标记或置顶）
     */
    HIGH("高优先级"),

    /**
     * 中优先级（普通展示）
     */
    MEDIUM("中优先级"),

    /**
     * 低优先级（普通展示）
     */
    LOW("低优先级");

    /**
     * 优先级描述
     */
    private final String description;

    /**
     * 根据字符串获取优先级
     *
     * @param value 优先级字符串
     * @return 优先级枚举
     */
    public static Priority fromString(String value) {
        if (value == null) {
            return MEDIUM;
        }

        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value.trim())) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
