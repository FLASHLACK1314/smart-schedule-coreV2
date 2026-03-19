package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyChatResponseDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyConversationDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyMessageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Dify 聊天服务接口
 *
 * @author flash
 */
public interface DifyChatService {

    /**
     * 发送消息（创建或继续会话）- 阻塞模式
     *
     * @param userUuid 用户UUID
     * @param getData  聊天请求
     * @return 聊天响应
     */
    DifyChatResponseDTO sendMessage(String userUuid, DifyChatVO getData);

    /**
     * 流式发送消息（返回 SSE 事件流）
     * <p>
     * 推荐用于涉及 MCP 工具调用等长时间操作的场景，避免超时
     *
     * @param userUuid 用户UUID
     * @param getData  聊天请求
     * @return SSE Emitter，用于推送流式事件
     */
    SseEmitter sendMessageStream(String userUuid, DifyChatVO getData);

    /**
     * 获取用户的会话列表
     *
     * @param userUuid 用户UUID
     * @return 会话列表
     */
    List<DifyConversationDTO> getConversations(String userUuid);

    /**
     * 获取会话历史消息
     *
     * @param userUuid       用户UUID
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<DifyMessageDTO> getMessages(String userUuid, String conversationId);

    /**
     * 删除会话
     *
     * @param userUuid       用户UUID
     * @param conversationId 会话ID
     */
    void deleteConversation(String userUuid, String conversationId);

    /**
     * 重命名会话
     *
     * @param userUuid       用户UUID
     * @param conversationId 会话ID
     * @param name           新名称
     */
    void renameConversation(String userUuid, String conversationId, String name);
}
