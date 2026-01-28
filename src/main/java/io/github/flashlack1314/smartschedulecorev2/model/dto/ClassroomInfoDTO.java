package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教室信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ClassroomInfoDTO {
    /**
     * 教室uuid
     */
    private String classroomUuid;
    /**
     * 教学楼名称
     */
    private String buildingName;
    /**
     * 教室名称
     */
    private String classroomName;
    /**
     * 教室容量
     */
    private Integer capacity;
    /**
     * 教室类型名称
     */
    private String typeName;
    /**
     * 教室类型描述
     */
    private String typeDescription;
}
