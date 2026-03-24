package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 今日课程DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TodayCourseDTO {

    /**
     * 排课记录ID
     */
    private String id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 班级名称(多个班级用逗号分隔)
     */
    private String className;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 开始节次
     */
    private Integer startSection;

    /**
     * 结束节次
     */
    private Integer endSection;

    /**
     * 开始时间(HH:mm)
     */
    private String startTime;

    /**
     * 结束时间(HH:mm)
     */
    private String endTime;
}
