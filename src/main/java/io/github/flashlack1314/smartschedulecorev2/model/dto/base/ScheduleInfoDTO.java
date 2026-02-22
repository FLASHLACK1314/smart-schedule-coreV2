package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 排课记录信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ScheduleInfoDTO {
    /**
     * 排课记录UUID
     */
    private String scheduleUuid;
    /**
     * 学期名称
     */
    private String semesterName;
    /**
     * 教学班名称
     */
    private String teachingClassName;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 教师名称
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
     * 起始节次
     */
    private Integer sectionStart;
    /**
     * 结束节次
     */
    private Integer sectionEnd;
    /**
     * 上课周次 JSON数组字符串
     */
    private String weeksJson;
    /**
     * 累计学时 (单次学时 × 周次数)
     */
    private Integer creditHours;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
