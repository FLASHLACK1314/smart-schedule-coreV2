package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 今日课程响应DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TodayCourseResponseDTO {

    /**
     * 当前日期
     */
    private String date;

    /**
     * 星期几(1-7)
     */
    private Integer weekDay;

    /**
     * 今日课程列表
     */
    private List<TodayCourseDTO> courses;
}
