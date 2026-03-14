package io.github.flashlack1314.smartschedulecorev2.mcp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 排课预览数据传输对象
 * 用于暂存调课预览信息到 Redis
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class SchedulePreviewDTO {

    /**
     * 预览ID（Redis存储的key）
     */
    private String previewId;

    /**
     * 学期UUID
     */
    private String semesterUuid;

    /**
     * 原排课UUID
     */
    private String originalScheduleUuid;

    /**
     * 原排课信息
     */
    private ScheduleInfo originalSchedule;

    /**
     * 新排课信息
     */
    private ScheduleInfo newSchedule;

    /**
     * 冲突检测结果
     */
    private ConflictResult conflictResult;

    /**
     * 预览创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 排课信息内部类
     */
    @Data
    @Accessors(chain = true)
    public static class ScheduleInfo {
        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 教师姓名
         */
        private String teacherName;

        /**
         * 教室名称
         */
        private String classroomName;

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

        /**
         * 上课周次
         */
        private String weeksJson;
    }

    /**
     * 冲突结果内部类
     */
    @Data
    @Accessors(chain = true)
    public static class ConflictResult {
        /**
         * 是否有冲突
         */
        private Boolean hasConflict;

        /**
         * 冲突类型列表
         */
        private java.util.List<String> conflictTypes;

        /**
         * 冲突详情描述
         */
        private String conflictDescription;
    }

    /**
     * 排课选择项内部类
     * 用于多匹配场景下返回可选的排课列表
     */
    @Data
    @Accessors(chain = true)
    public static class ScheduleSelectionItem {
        /**
         * 选择码（用于后续选择操作）
         */
        private String selectionCode;

        /**
         * 排课UUID
         */
        private String scheduleUuid;

        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 教师姓名
         */
        private String teacherName;

        /**
         * 教室名称
         */
        private String classroomName;

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

        /**
         * 上课周次
         */
        private String weeksJson;
    }
}