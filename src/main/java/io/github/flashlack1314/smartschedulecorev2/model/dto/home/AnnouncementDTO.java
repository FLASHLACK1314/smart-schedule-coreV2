package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统公告DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AnnouncementDTO {

    /**
     * 公告ID
     */
    private String id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 优先级(HIGH/MEDIUM/LOW)
     */
    private String priority;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 相对时间描述(如"3天前")
     */
    private String relativeTime;
}
