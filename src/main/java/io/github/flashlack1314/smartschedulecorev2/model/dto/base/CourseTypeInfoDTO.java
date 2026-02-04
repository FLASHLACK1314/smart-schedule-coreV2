package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 课程类型信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class CourseTypeInfoDTO {
    /**
     * 课程类型UUID
     */
    private String courseTypeUuid;
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 类型描述
     */
    private String typeDescription;
}
