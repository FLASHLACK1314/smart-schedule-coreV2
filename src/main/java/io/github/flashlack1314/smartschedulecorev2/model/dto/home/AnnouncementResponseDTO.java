package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统公告响应DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AnnouncementResponseDTO {

    /**
     * 公告列表
     */
    private List<AnnouncementDTO> announcements;
}
