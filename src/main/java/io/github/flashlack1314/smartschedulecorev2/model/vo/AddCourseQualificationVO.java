package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加课程教师资格关联VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddCourseQualificationVO {
    /**
     * 课程UUID
     */
    private String courseUuid;

    /**
     * 教师UUID
     */
    private String teacherUuid;
}
