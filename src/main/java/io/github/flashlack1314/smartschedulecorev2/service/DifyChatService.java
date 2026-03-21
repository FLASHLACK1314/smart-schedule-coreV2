package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import io.github.imfangs.dify.client.model.chat.Conversation;
import io.github.imfangs.dify.client.model.chat.MessageListResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Dify 聊天服务接口
 * <p>
 * 所有响应类型均使用 dify-java-client 库的原始类型，确保响应格式与 Dify 官方 API 完全一致。
 *
 * @author flash
 */
public interface DifyChatService {

    /**
     * 发送消息（创建或继续会话）- 阻塞模式
     * <p>
     * 返回完整的 {@link ChatMessageResponse}，包含：
     * <ul>
     *   <li>message_id - 消息唯一标识</li>
     *   <li>conversation_id - 会话ID</li>
     *   <li>mode - 响应模式（chat/completion）</li>
     *   <li>answer - AI 回答内容</li>
     *   <li>metadata - 元数据（包含 usage 等信息）</li>
     *   <li>created_at - 创建时间戳</li>
     * </ul>
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     * @param getData  聊天请求
     * @return Dify 官方格式的聊天响应
     */
    ChatMessageResponse sendMessage(String userUuid, UserType userType, DifyChatVO getData);

    /**
     * 流式发送消息（返回 SSE 事件流）
     * <p>
     * 推荐用于涉及 MCP 工具调用等长时间操作的场景，避免超时。
     * <p>
     * SSE 事件类型与 Dify 官方完全一致：
     * <ul>
     *   <li>workflow_started - 工作流开始处理</li>
     *   <li>node_started - 节点开始执行</li>
     *   <li>node_finished - 节点执行结束</li>
     *   <li>message - 消息片段</li>
     *   <li>message_end - 消息完成</li>
     *   <li>workflow_finished - 工作流执行结束</li>
     *   <li>ping - 心跳事件</li>
     *   <li>error - 错误事件</li>
     * </ul>
     * <p>
     * 所有事件均包含 task_id 字段用于请求跟踪。
     *
     * @param userUuid 用户UUID
     * @param userType 用户类型
     * @param getData  聊天请求
     * @return SSE Emitter，用于推送流式事件
     */
    SseEmitter sendMessageStream(String userUuid, UserType userType, DifyChatVO getData);

    /**
     * 获取用户的会话列表
     * <p>
     * 返回 Dify 官方格式的 {@link Conversation} 列表，每个会话包含：
     * <ul>
     *   <li>id - 会话ID</li>
     *   <li>name - 会话名称</li>
     *   <li>inputs - 输入参数</li>
     *   <li>status - 会话状态（normal/completed等）</li>
     *   <li>created_at - 创建时间戳</li>
     *   <li>updated_at - 更新时间戳</li>
     * </ul>
     *
     * @param userUuid 用户UUID
     * @return Dify 官方格式的会话列表
     */
    List<Conversation> getConversations(String userUuid);

    /**
     * 获取会话历史消息
     * <p>
     * 返回 Dify 官方格式的 {@link MessageListResponse.Message} 列表，每条消息包含：
     * <ul>
     *   <li>id - 消息ID</li>
     *   <li>conversation_id - 会话ID</li>
     *   <li>query - 用户提问内容</li>
     *   <li>answer - AI 回答内容</li>
     *   <li>feedback - 反馈信息</li>
     *   <li>created_at - 创建时间戳</li>
     * </ul>
     *
     * @param userUuid       用户UUID
     * @param conversationId 会话ID
     * @return Dify 官方格式的消息列表
     */
    List<MessageListResponse.Message> getMessages(String userUuid, String conversationId);

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
