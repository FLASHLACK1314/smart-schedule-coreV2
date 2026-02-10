package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加课程类型-教室类型关联VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddCourseClassroomTypeVO {
    /**
     * 课程类型UUID
     */
    private String courseTypeUuid;

    /**
     * 教室类型UUID
     */
    private String classroomTypeUuid;
}
