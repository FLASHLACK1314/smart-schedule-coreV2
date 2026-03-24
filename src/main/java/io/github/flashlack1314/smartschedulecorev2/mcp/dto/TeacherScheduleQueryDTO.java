package io.github.flashlack1314.smartschedulecorev2.mcp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 教师课表查询结果 DTO
 * MCP 工具 queryTeacherScheduleByTime 的返回值
 * Spring AI 会自动序列化为 JSON 返回给 Dify
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TeacherScheduleQueryDTO {

    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 错误消息（仅 success=false 时有值）
     */
    private String errorMessage;

    /**
     * 查询结果教师列表
     */
    private List<TeacherSchedule> teachers;

    /**
     * 单个教师的课表数据
     */
    @Data
    @Accessors(chain = true)
    public static class TeacherSchedule {
        /**
         * 教师UUID
         */
        private String teacherUuid;

        /**
         * 教师姓名
         */
        private String teacherName;

        /**
         * 教师工号
         */
        private String teacherNum;

        /**
         * 筛选条件描述（如 "全部" 或 "周五"）
         */
        private String filterDescription;

        /**
         * 该教师排课数量
         */
        private int scheduleCount;

        /**
         * 排课列表
         */
        private List<ScheduleItem> schedules;
    }

    /**
     * 单条排课记录
     */
    @Data
    @Accessors(chain = true)
    public static class ScheduleItem {
        /**
         * 排课UUID
         */
        private String scheduleUuid;

        /**
         * 课程名称
         */
        private String courseName;

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
         * 上课周次 JSON 数组字符串，如 "[1,2,3,4,5]"
         */
        private String weeksJson;
    }
}
