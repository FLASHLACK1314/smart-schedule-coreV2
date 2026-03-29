package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 成绩统计DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ScoreStatisticsDTO {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 学生总数
     */
    private Integer totalStudents;

    /**
     * 已录入成绩数
     */
    private Integer enteredCount;

    /**
     * 平均分
     */
    private BigDecimal averageScore;

    /**
     * 最高分
     */
    private BigDecimal maxScore;

    /**
     * 最低分
     */
    private BigDecimal minScore;

    /**
     * 及格人数（60分以上）
     */
    private Integer passCount;

    /**
     * 及格率（百分比）
     */
    private BigDecimal passRate;

    /**
     * 优秀人数（90分以上）
     */
    private Integer excellentCount;

    /**
     * 优秀率（百分比）
     */
    private BigDecimal excellentRate;

    /**
     * 平均绩点
     */
    private BigDecimal averageGradePoint;
}
