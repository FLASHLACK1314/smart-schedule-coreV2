package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学楼信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class BuildingInfoDTO {
    /**
     * 教学楼uuid
     */
    private String buildingUuid;
    /**
     * 教学楼编号
     */
    private String buildingNum;
    /**
     * 教学楼名称
     */
    private String buildingName;

}
