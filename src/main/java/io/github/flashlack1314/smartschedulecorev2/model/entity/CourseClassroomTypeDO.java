package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程类型-教室类型关联DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_course_classroom_type")
public class CourseClassroomTypeDO {
    /**
     * 关联关系UUID
     */
    @TableId
    private String relationUuid;

    /**
     * 课程类型UUID
     */
    @TableField("course_type_uuid")
    private String courseTypeUuid;

    /**
     * 教室类型UUID
     */
    @TableField("classroom_type_uuid")
    private String classroomTypeUuid;
}