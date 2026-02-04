package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学院信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class DepartmentInfoDTO {
    /**
     * 学院UUID
     */
    private String departmentUuid;
    /**
     * 学院名称
     */
    private String departmentName;
}
