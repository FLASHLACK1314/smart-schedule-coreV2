package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 排课冲突记录信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ScheduleConflictInfoDTO {
    /**
     * 冲突记录UUID
     */
    private String conflictUuid;
    /**
     * 学期名称
     */
    private String semesterName;
    /**
     * 排课记录A的UUID
     */
    private String scheduleUuidA;
    /**
     * 排课记录B的UUID
     */
    private String scheduleUuidB;
    /**
     * 冲突类型
     */
    private String conflictType;
    /**
     * 严重程度
     */
    private Integer severity;
    /**
     * 冲突描述
     */
    private String description;
}
