package io.github.flashlack1314.smartschedulecorev2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author flash
 */
@Getter
@AllArgsConstructor
public enum ActionType {

    /**
     * 完成了智能排课
     */
    AUTO_SCHEDULE("完成了智能排课"),

    /**
     * 手动添加了排课
     */
    MANUAL_SCHEDULE("手动添加了排课"),

    /**
     * 修改了排课信息
     */
    UPDATE_SCHEDULE("修改了排课信息"),

    /**
     * 删除了排课记录
     */
    DELETE_SCHEDULE("删除了排课记录"),

    /**
     * 添加了新教师
     */
    ADD_TEACHER("添加了新教师"),

    /**
     * 更新了教师信息
     */
    UPDATE_TEACHER("更新了教师信息"),

    /**
     * 添加了新课程
     */
    ADD_COURSE("添加了新课程"),

    /**
     * 更新了课程信息
     */
    UPDATE_COURSE("更新了课程信息"),

    /**
     * 添加了新教室
     */
    ADD_CLASSROOM("添加了新教室"),

    /**
     * 导出了课表数据
     */
    EXPORT_TIMETABLE("导出了课表数据"),

    /**
     * 导入了基础数据
     */
    IMPORT_DATA("导入了基础数据"),

    /**
     * 登录了系统
     */
    LOGIN("登录了系统");

    /**
     * 操作描述文本
     */
    private final String actionText;

    /**
     * 根据字符串获取操作类型
     *
     * @param value 操作类型字符串
     * @return 操作类型枚举
     */
    public static ActionType fromString(String value) {
        if (value == null) {
            return null;
        }

        for (ActionType type : ActionType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的操作类型: " + value);
    }
}
