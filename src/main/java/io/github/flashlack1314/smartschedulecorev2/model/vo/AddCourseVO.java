package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 添加课程VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddCourseVO {
    /**
     * 课程UUID（更新时需要）
     */
    private String courseUuid;

    /**
     * 课程编号（唯一）
     */
    private String courseNum;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型UUID
     */
    private String courseTypeUuid;

    /**
     * 课程学分（支持半分）
     */
    private BigDecimal courseCredit;

    /**
     * 课程学时（总课时数）
     */
    private Integer courseHours;
}
