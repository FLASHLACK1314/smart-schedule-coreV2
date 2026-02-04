package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 专业信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class MajorInfoDTO {
    /**
     * 专业UUID
     */
    private String majorUuid;
    /**
     * 学院名称
     */
    private String departmentName;
    /**
     * 专业编号
     */
    private String majorNum;
    /**
     * 专业名称
     */
    private String majorName;
}
