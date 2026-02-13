package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学班-行政班关联信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TeachingClassClassInfoDTO {
    /**
     * 关联关系UUID
     */
    private String teachingClassClassUuid;

    // ========== 教学班信息 ==========
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 教学班名称
     */
    private String teachingClassName;

    // ========== 课程信息（通过教学班关联） ==========
    /**
     * 课程UUID
     */
    private String courseUuid;

    /**
     * 课程名称
     */
    private String courseName;

    // ========== 教师信息（通过教学班关联） ==========
    /**
     * 教师UUID
     */
    private String teacherUuid;

    /**
     * 教师姓名
     */
    private String teacherName;

    // ========== 行政班级信息 ==========
    /**
     * 行政班级UUID
     */
    private String classUuid;

    /**
     * 行政班级名称
     */
    private String className;

    // ========== 专业信息（通过行政班级关联） ==========
    /**
     * 专业UUID
     */
    private String majorUuid;

    /**
     * 专业名称
     */
    private String majorName;

    // ========== 学院信息（通过专业关联） ==========
    /**
     * 学院UUID
     */
    private String departmentUuid;

    /**
     * 学院名称
     */
    private String departmentName;
}
