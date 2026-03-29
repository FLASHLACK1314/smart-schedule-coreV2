package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 批量添加成绩VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class BatchAddScoreVO {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 学期UUID
     */
    private String semesterUuid;

    /**
     * 成绩列表
     */
    private List<ScoreItem> scoreItems;

    /**
     * 成绩项
     */
    @Data
    @Accessors(chain = true)
    public static class ScoreItem {
        /**
         * 学生UUID
         */
        private String studentUuid;

        /**
         * 平时成绩（0-100）
         */
        private java.math.BigDecimal usualScore;

        /**
         * 期中成绩（0-100）
         */
        private java.math.BigDecimal midtermScore;

        /**
         * 期末成绩（0-100）
         */
        private java.math.BigDecimal finalScore;

        /**
         * 备注
         */
        private String remark;
    }
}
