package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教室类型信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ClassroomTypeInfoDTO {
    /**
     * 教室类型UUID
     */
    private String classroomTypeUuid;
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 类型描述
     */
    private String typeDescription;
}
