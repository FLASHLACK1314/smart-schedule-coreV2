package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学期信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class SemesterInfoDTO {
    /**
     * 学期UUID
     */
    private String semesterUuid;
    /**
     * 学期名称
     */
    private String semesterName;
    /**
     * 学期周数
     */
    private Integer semesterWeeks;
}
