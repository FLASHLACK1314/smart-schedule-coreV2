package io.github.flashlack1314.smartschedulecorev2.mcp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 时间槽可用性检测结果 DTO
 * MCP 工具 checkTimeSlotAvailability 的返回值
 * Spring AI 会自动序列化为 JSON 返回给 Dify
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TimeSlotCheckDTO {

    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 错误消息（仅 success=false 时有值）
     */
    private String errorMessage;

    /**
     * 检测的时间槽信息
     */
    private TimeSlotInfo timeSlot;

    /**
     * 检测结果列表（教室和教师各一项）
     */
    private List<CheckResult> results;

    /**
     * 是否有任何冲突
     */
    private boolean hasConflict;

    /**
     * 冲突类型汇总列表（如 ["教室冲突", "教师冲突"]）
     */
    private List<String> conflictTypes;

    /**
     * 检测的时间槽
     */
    @Data
    @Accessors(chain = true)
    public static class TimeSlotInfo {
        /**
         * 星期几 (1-7)
         */
        private Integer dayOfWeek;

        /**
         * 星期几中文描述
         */
        private String dayOfWeekStr;

        /**
         * 起始节次
         */
        private Integer sectionStart;

        /**
         * 结束节次
         */
        private Integer sectionEnd;
    }

    /**
     * 单项检测结果（教室或教师）
     */
    @Data
    @Accessors(chain = true)
    public static class CheckResult {
        /**
         * 检测类型："classroom" 或 "teacher"
         */
        private String checkType;

        /**
         * 检测对象名称
         */
        private String name;

        /**
         * 是否找到该对象（教师/教室是否存在）
         */
        private boolean found;

        /**
         * 是否有冲突
         */
        private boolean hasConflict;

        /**
         * 冲突排课列表（仅 hasConflict=true 时有值）
         */
        private List<ConflictItem> conflicts;
    }

    /**
     * 冲突排课项
     */
    @Data
    @Accessors(chain = true)
    public static class ConflictItem {
        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 教师/教室名称
         */
        private String relatedName;
    }
}
