package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程教师资格关联信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class CourseQualificationInfoDTO {
    /**
     * 关联关系UUID
     */
    private String courseQualificationUuid;

    /**
     * 课程UUID
     */
    private String courseUuid;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师UUID
     */
    private String teacherUuid;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师职称
     */
    private String teacherTitle;

    /**
     * 学院UUID
     */
    private String departmentUuid;

    /**
     * 学院名称
     */
    private String departmentName;
}
