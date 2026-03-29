package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ScoreInfoDTO {
    /**
     * 成绩UUID
     */
    private String scoreUuid;

    /**
     * 学生信息
     */
    private StudentInfoDTO studentInfo;

    /**
     * 教学班信息
     */
    private TeachingClassInfoDTO teachingClassInfo;

    /**
     * 学期信息
     */
    private SemesterInfoDTO semesterInfo;

    /**
     * 平时成绩（0-100）
     */
    private BigDecimal usualScore;

    /**
     * 期中成绩（0-100）
     */
    private BigDecimal midtermScore;

    /**
     * 期末成绩（0-100）
     */
    private BigDecimal finalScore;

    /**
     * 总评成绩（0-100）
     */
    private BigDecimal totalScore;

    /**
     * 绩点（0-5.0）
     */
    private BigDecimal gradePoint;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
