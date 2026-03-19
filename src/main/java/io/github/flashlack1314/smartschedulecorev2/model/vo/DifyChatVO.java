package io.github.flashlack1314.smartschedulecorev2.model.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Dify 聊天请求 VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class DifyChatVO {

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String query;

    /**
     * 会话ID（可选，首次为空，后续对话传入之前返回的会话ID）
     */
    private String conversationId;
}
