package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 课程DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_course")
public class CourseDO {

    /**
     * 课程UUID
     */
    @TableId
    private String courseUuid;

    /**
     * 课程编号 (唯一编码)
     */
    @TableField("course_num")
    private String courseNum;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 课程类型UUID
     */
    @TableField("course_type_uuid")
    private String courseTypeUuid;

    /**
     * 课程学分 (支持半分)
     */
    @TableField("course_credit")
    private BigDecimal courseCredit;

    /**
     * 具有教授资格的老师UUID列表 (JSONB 数组通用存放)
     */
    @TableField(value = "qualified_teacher_uuids")
    private String qualifiedTeacherUuids;
}
