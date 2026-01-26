package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教师DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_teacher")
public class TeacherDO {

    /**
     * 教师UUID
     */
    @TableId
    private String teacherUuid;

    /**
     * 教师编号（唯一工号）
     */
    @TableField("teacher_num")
    private String teacherNum;

    /**
     * 教师名称
     */
    @TableField("teacher_name")
    private String teacherName;

    /**
     * 职称
     */
    @TableField("title")
    private String title;

    /**
     * 密码
     */
    @TableField("teacher_password")
    private String teacherPassword;

    /**
     * 每周最高授课时长
     */
    @TableField("max_hours_per_week")
    private Integer maxHoursPerWeek;

    /**
     * 喜欢时间 (字符串格式)
     */
    @TableField(value = "like_time")
    private String likeTime;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;
}
