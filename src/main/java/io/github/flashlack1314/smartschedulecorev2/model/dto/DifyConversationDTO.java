package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dify 会话信息 DTO
 *
 * @author flash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyConversationDTO {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
