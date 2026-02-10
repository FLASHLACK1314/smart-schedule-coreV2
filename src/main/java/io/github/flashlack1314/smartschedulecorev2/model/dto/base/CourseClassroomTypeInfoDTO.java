package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程类型-教室类型关联信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class CourseClassroomTypeInfoDTO {
    /**
     * 关联关系UUID
     */
    private String relationUuid;

    /**
     * 课程类型UUID
     */
    private String courseTypeUuid;

    /**
     * 课程类型名称
     */
    private String courseTypeName;

    /**
     * 教室类型UUID
     */
    private String classroomTypeUuid;

    /**
     * 教室类型名称
     */
    private String classroomTypeName;
}
