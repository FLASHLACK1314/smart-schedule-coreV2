package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyChatResponseDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyConversationDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyMessageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import io.github.flashlack1314.smartschedulecorev2.service.DifyChatService;
import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.callback.ChatflowStreamCallback;
import io.github.imfangs.dify.client.exception.DifyApiException;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import io.github.imfangs.dify.client.model.chat.Conversation;
import io.github.imfangs.dify.client.model.chat.ConversationListResponse;
import io.github.imfangs.dify.client.model.chat.MessageListResponse;
import io.github.imfangs.dify.client.event.ErrorEvent;
import io.github.imfangs.dify.client.event.MessageEndEvent;
import io.github.imfangs.dify.client.event.MessageEvent;
import io.github.imfangs.dify.client.event.NodeStartedEvent;
import io.github.imfangs.dify.client.event.WorkflowFinishedEvent;
import io.github.imfangs.dify.client.event.WorkflowStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dify 聊天服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyChatServiceImpl implements DifyChatService {

    private final DifyChatflowClient difyChatflowClient;

    @Override
    public DifyChatResponseDTO sendMessage(String userUuid, DifyChatVO getData) {
        log.info("发送 Dify 消息 - userUuid: {}, conversationId: {}", userUuid, getData.getConversationId());

        try {
            // 构建聊天消息请求
            ChatMessage.ChatMessageBuilder builder = ChatMessage.builder()
                    .query(getData.getQuery())
                    .user(userUuid);

            // 如果有会话ID，则继续该会话
            if (getData.getConversationId() != null && !getData.getConversationId().isEmpty()) {
                builder.conversationId(getData.getConversationId());
            }

            ChatMessage chatMessage = builder.build();

            // 使用阻塞模式发送消息
            ChatMessageResponse response = difyChatflowClient.sendChatMessage(chatMessage);

            // 构建响应
            return DifyChatResponseDTO.builder()
                    .messageId(response.getMessageId())
                    .conversationId(response.getConversationId())
                    .answer(response.getAnswer())
                    .build();

        } catch (IOException | DifyApiException e) {
            log.error("Dify API 调用失败 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public SseEmitter sendMessageStream(String userUuid, DifyChatVO getData) {
        log.info("流式发送 Dify 消息 - userUuid: {}, conversationId: {}", userUuid, getData.getConversationId());

        // 创建 SSE Emitter，设置 5 分钟超时
        SseEmitter emitter = new SseEmitter(300000L);

        // 构建聊天消息请求
        ChatMessage.ChatMessageBuilder builder = ChatMessage.builder()
                .query(getData.getQuery())
                .user(userUuid);

        // 如果有会话ID，则继续该会话
        if (getData.getConversationId() != null && !getData.getConversationId().isEmpty()) {
            builder.conversationId(getData.getConversationId());
        }

        ChatMessage chatMessage = builder.build();

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时 - userUuid: {}", userUuid);
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接完成 - userUuid: {}", userUuid));
        emitter.onError(throwable -> log.error("SSE 连接错误 - userUuid: {}, error: {}", userUuid, throwable.getMessage()));

        // 使用流式模式发送消息
        try {
            difyChatflowClient.sendChatMessageStream(chatMessage, new ChatflowStreamCallback() {
                final StringBuilder fullAnswer = new StringBuilder();

                @Override
                public void onWorkflowStarted(WorkflowStartedEvent event) {
                    log.debug("工作流开始 - workflowRunId: {}", event.getWorkflowRunId());
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("workflow_run_id", event.getWorkflowRunId());
                        data.put("message", "工作流开始处理");
                        emitter.send(SseEmitter.event()
                                .name("workflow_started")
                                .data(data));
                    } catch (IOException e) {
                        log.error("发送 workflow_started 事件失败", e);
                    }
                }

                @Override
                public void onNodeStarted(NodeStartedEvent event) {
                    NodeStartedEvent.NodeStartedData nodeData = event.getData();
                    log.debug("节点开始 - node: {}, title: {}",
                            nodeData.getNodeId(), nodeData.getTitle());
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("node_id", nodeData.getNodeId());
                        data.put("node_type", nodeData.getNodeType());
                        data.put("title", nodeData.getTitle());
                        emitter.send(SseEmitter.event()
                                .name("node_started")
                                .data(data));
                    } catch (IOException e) {
                        log.error("发送 node_started 事件失败", e);
                    }
                }

                @Override
                public void onMessage(MessageEvent event) {
                    log.debug("收到消息片段 - answer: {}", event.getAnswer());
                    fullAnswer.append(event.getAnswer());
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("answer", event.getAnswer());
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(data));
                    } catch (IOException e) {
                        log.error("发送 message 事件失败", e);
                    }
                }

                @Override
                public void onMessageEnd(MessageEndEvent event) {
                    log.info("消息完成 - messageId: {}, conversationId: {}", event.getMessageId(), event.getConversationId());
                    sendDoneEvent(event.getMessageId(), event.getConversationId());
                }

                @Override
                public void onWorkflowFinished(WorkflowFinishedEvent event) {
                    log.info("工作流完成 - status: {}", event.getData() != null ? event.getData().getStatus() : "unknown");
                    // Chatflow 在某些情况下只触发 workflow_finished 而不触发 message_end
                    // 从 outputs 中获取 message_id 和 conversation_id
                    String messageId = null;
                    String conversationId = null;
                    if (event.getData() != null && event.getData().getOutputs() != null) {
                        Map<String, Object> outputs = event.getData().getOutputs();
                        messageId = (String) outputs.get("message_id");
                        conversationId = (String) outputs.get("conversation_id");
                    }
                    sendDoneEvent(messageId, conversationId);
                }

                private void sendDoneEvent(String messageId, String conversationId) {
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("message_id", messageId);
                        data.put("conversation_id", conversationId);
                        data.put("answer", fullAnswer.toString());
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data(data));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送 done 事件失败", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(ErrorEvent event) {
                    log.error("Dify 流式响应错误 - code: {}, message: {}", event.getCode(), event.getMessage());
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("code", event.getCode());
                        data.put("message", event.getMessage());
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(data));
                        emitter.completeWithError(new RuntimeException(event.getMessage()));
                    } catch (IOException e) {
                        log.error("发送 error 事件失败", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("Dify 流式响应异常", throwable);
                    emitter.completeWithError(throwable);
                }
            });
        } catch (Exception e) {
            log.error("启动流式消息失败 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    public List<DifyConversationDTO> getConversations(String userUuid) {
        log.info("获取 Dify 会话列表 - userUuid: {}", userUuid);

        try {
            // 使用正确的 API 签名: getConversations(user, lastId, limit, sortBy)
            ConversationListResponse response = difyChatflowClient.getConversations(userUuid, null, 20, "-updated_at");

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .map(this::convertToConversationDTO)
                    .collect(Collectors.toList());

        } catch (IOException | DifyApiException e) {
            log.error("获取 Dify 会话列表失败 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new BusinessException("获取会话列表失败", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public List<DifyMessageDTO> getMessages(String userUuid, String conversationId) {
        log.info("获取 Dify 会话消息 - userUuid: {}, conversationId: {}", userUuid, conversationId);

        try {
            // 使用正确的 API 签名: getMessages(conversationId, user, firstId, limit)
            MessageListResponse response = difyChatflowClient.getMessages(conversationId, userUuid, null, 100);

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .map(this::convertToMessageDTO)
                    .collect(Collectors.toList());

        } catch (IOException | DifyApiException e) {
            log.error("获取 Dify 会话消息失败 - userUuid: {}, conversationId: {}, error: {}",
                    userUuid, conversationId, e.getMessage(), e);
            throw new BusinessException("获取会话消息失败", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public void deleteConversation(String userUuid, String conversationId) {
        log.info("删除 Dify 会话 - userUuid: {}, conversationId: {}", userUuid, conversationId);

        try {
            difyChatflowClient.deleteConversation(conversationId, userUuid);
            log.info("Dify 会话删除成功 - conversationId: {}", conversationId);

        } catch (IOException | DifyApiException e) {
            log.error("删除 Dify 会话失败 - userUuid: {}, conversationId: {}, error: {}",
                    userUuid, conversationId, e.getMessage(), e);
            throw new BusinessException("删除会话失败", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public void renameConversation(String userUuid, String conversationId, String name) {
        log.info("重命名 Dify 会话 - userUuid: {}, conversationId: {}, name: {}", userUuid, conversationId, name);

        try {
            // 使用正确的 API 签名: renameConversation(conversationId, name, autoGenerate, user)
            difyChatflowClient.renameConversation(conversationId, name, false, userUuid);
            log.info("Dify 会话重命名成功 - conversationId: {}, name: {}", conversationId, name);

        } catch (IOException | DifyApiException e) {
            log.error("重命名 Dify 会话失败 - userUuid: {}, conversationId: {}, error: {}",
                    userUuid, conversationId, e.getMessage(), e);
            throw new BusinessException("重命名会话失败", ErrorCode.OPERATION_FAILED);
        }
    }

    /**
     * 将 Dify Conversation 转换为 DTO
     */
    private DifyConversationDTO convertToConversationDTO(Conversation conversation) {
        return DifyConversationDTO.builder()
                .conversationId(conversation.getId())
                .name(conversation.getName())
                .createdAt(convertToLocalDateTime(conversation.getCreatedAt()))
                .updatedAt(convertToLocalDateTime(conversation.getUpdatedAt()))
                .build();
    }

    /**
     * 将 Dify Message 转换为 DTO
     * 注意: MessageListResponse.Message 没有 content 字段，需要根据 role 决定使用 answer 还是 query
     */
    private DifyMessageDTO convertToMessageDTO(MessageListResponse.Message message) {
        // 根据消息来源决定内容
        // assistant 角色使用 answer，user 角色使用 query
        String content = message.getAnswer() != null ? message.getAnswer() : message.getQuery();

        return DifyMessageDTO.builder()
                .messageId(message.getId())
                .conversationId(message.getConversationId())
                .content(content)
                .role(message.getAnswer() != null ? "assistant" : "user")
                .createdAt(convertToLocalDateTime(message.getCreatedAt()))
                .build();
    }

    /**
     * 将 Unix 时间戳（秒）转换为 LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneId.systemDefault().getRules().getOffset(Instant.ofEpochSecond(timestamp)));
    }
}
