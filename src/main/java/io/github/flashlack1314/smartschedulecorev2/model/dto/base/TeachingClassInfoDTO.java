package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学班信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TeachingClassInfoDTO {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 教师名称
     */
    private String teacherName;
    /**
     * 学期名称
     */
    private String semesterName;
    /**
     * 教学班名称
     */
    private String teachingClassName;
    /**
     * 教学班学时 (排课记录累计)
     */
    private Integer teachingClassHours;
}
