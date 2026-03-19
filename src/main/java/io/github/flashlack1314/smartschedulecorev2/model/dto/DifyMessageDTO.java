package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dify 消息信息 DTO
 *
 * @author flash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyMessageDTO {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息角色（user/assistant）
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
