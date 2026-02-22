package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 课程信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class CourseInfoDTO {
    /**
     * 课程UUID
     */
    private String courseUuid;
    /**
     * 课程编号
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
     * 课程类型名称
     */
    private String courseTypeName;
    /**
     * 课程学分
     */
    private BigDecimal courseCredit;

    /**
     * 课程学时 (总课时数)
     */
    private Integer courseHours;
}
