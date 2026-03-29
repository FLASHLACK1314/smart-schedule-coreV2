package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 添加成绩VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddScoreVO {
    /**
     * 学生UUID
     */
    private String studentUuid;

    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 学期UUID
     */
    private String semesterUuid;

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
     * 备注
     */
    private String remark;
}
