package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 更新成绩VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class UpdateScoreVO {
    /**
     * 成绩UUID
     */
    private String scoreUuid;

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
