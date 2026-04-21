package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 排课冲突记录信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ScheduleConflictInfoDTO {
    /**
     * 冲突记录UUID
     */
    private String conflictUuid;
    /**
     * 学期名称
     */
    private String semesterName;
    /**
     * 排课记录A的UUID
     */
    private String scheduleUuidA;
    /**
     * 排课记录B的UUID
     */
    private String scheduleUuidB;
    /**
     * 冲突类型
     */
    private String conflictType;
    /**
     * 严重程度
     */
    private Integer severity;
    /**
     * 冲突描述
     */
    private String description;

    // ===== 排课A信息 =====
    /**
     * 排课A - 教学班名称
     */
    private String teachingClassNameA;

    /**
     * 排课A - 课程名称
     */
    private String courseNameA;

    /**
     * 排课A - 教师名称
     */
    private String teacherNameA;

    /**
     * 排课A - 教室名称
     */
    private String classroomNameA;

    /**
     * 排课A - 星期几
     */
    private Integer dayOfWeekA;

    /**
     * 排课A - 起始节次
     */
    private Integer sectionStartA;

    /**
     * 排课A - 结束节次
     */
    private Integer sectionEndA;

    // ===== 排课B信息 =====
    /**
     * 排课B - 教学班名称
     */
    private String teachingClassNameB;

    /**
     * 排课B - 课程名称
     */
    private String courseNameB;

    /**
     * 排课B - 教师名称
     */
    private String teacherNameB;

    /**
     * 排课B - 教室名称
     */
    private String classroomNameB;

    /**
     * 排课B - 星期几
     */
    private Integer dayOfWeekB;

    /**
     * 排课B - 起始节次
     */
    private Integer sectionStartB;

    /**
     * 排课B - 结束节次
     */
    private Integer sectionEndB;
}
