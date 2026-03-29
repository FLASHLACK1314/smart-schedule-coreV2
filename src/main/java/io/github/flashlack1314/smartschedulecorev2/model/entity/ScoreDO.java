package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_score")
public class ScoreDO {

    /**
     * 成绩UUID（主键）
     */
    @TableId
    private String scoreUuid;

    /**
     * 学生UUID
     */
    @TableField("student_uuid")
    private String studentUuid;

    /**
     * 教学班UUID
     */
    @TableField("teaching_class_uuid")
    private String teachingClassUuid;

    /**
     * 学期UUID
     */
    @TableField("semester_uuid")
    private String semesterUuid;

    /**
     * 平时成绩（0-100）
     */
    @TableField("usual_score")
    private BigDecimal usualScore;

    /**
     * 期中成绩（0-100）
     */
    @TableField("midterm_score")
    private BigDecimal midtermScore;

    /**
     * 期末成绩（0-100）
     */
    @TableField("final_score")
    private BigDecimal finalScore;

    /**
     * 总评成绩（0-100）
     */
    @TableField("total_score")
    private BigDecimal totalScore;

    /**
     * 绩点（0-5.0）
     */
    @TableField("grade_point")
    private BigDecimal gradePoint;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
