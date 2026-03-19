package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dify 聊天响应 DTO
 *
 * @author flash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyChatResponseDTO {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID（用于后续对话）
     */
    private String conversationId;

    /**
     * AI 回复内容
     */
    private String answer;
}
