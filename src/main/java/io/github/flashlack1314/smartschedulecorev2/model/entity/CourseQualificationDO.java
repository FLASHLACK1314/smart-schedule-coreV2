package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程教师资格关联DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_course_qualification")
public class CourseQualificationDO {

    /**
     * 关联关系UUID
     */
    @TableId
    private String courseQualificationUuid;

    /**
     * 课程UUID
     */
    @TableField("course_uuid")
    private String courseUuid;

    /**
     * 教师UUID
     */
    @TableField("teacher_uuid")
    private String teacherUuid;
}
