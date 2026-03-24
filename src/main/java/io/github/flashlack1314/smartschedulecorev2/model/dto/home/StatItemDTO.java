package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计项DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class StatItemDTO {

    /**
     * 统计值
     */
    private Number value;

    /**
     * 变化率(百分比)
     */
    private Double changeRate;
}
