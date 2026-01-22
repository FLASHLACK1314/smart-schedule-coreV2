package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程类型DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_course_type")
public class CourseTypeDO {

    /**
     * 课程类型UUID
     */
    @TableId
    private String courseTypeUuid;

    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 类型描述
     */
    @TableField("type_description")
    private String typeDescription;
}