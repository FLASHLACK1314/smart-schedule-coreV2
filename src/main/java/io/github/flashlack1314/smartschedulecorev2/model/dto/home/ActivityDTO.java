package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动记录DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ActivityDTO {

    /**
     * 活动ID
     */
    private String id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 操作类型
     */
    private String actionType;

    /**
     * 操作描述文本
     */
    private String actionText;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
