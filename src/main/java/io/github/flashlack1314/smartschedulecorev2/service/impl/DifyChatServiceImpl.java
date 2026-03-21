package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.ErrorCode;
import com.xlf.utility.util.UuidUtil;
import com.xlf.utility.exception.BusinessException;
import io.github.flashlack1314.smartschedulecorev2.dao.DifyConversationDAO;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DifyConversationDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import io.github.flashlack1314.smartschedulecorev2.service.DifyChatService;
import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.callback.ChatflowStreamCallback;
import io.github.imfangs.dify.client.exception.DifyApiException;
import io.github.imfangs.dify.client.event.ErrorEvent;
import io.github.imfangs.dify.client.event.MessageEndEvent;
import io.github.imfangs.dify.client.event.MessageEvent;
import io.github.imfangs.dify.client.event.NodeFinishedEvent;
import io.github.imfangs.dify.client.event.NodeStartedEvent;
import io.github.imfangs.dify.client.event.PingEvent;
import io.github.imfangs.dify.client.event.WorkflowFinishedEvent;
import io.github.imfangs.dify.client.event.WorkflowStartedEvent;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import io.github.imfangs.dify.client.model.chat.Conversation;
import io.github.imfangs.dify.client.model.chat.ConversationListResponse;
import io.github.imfangs.dify.client.model.chat.MessageListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dify 聊天服务实现
 * <p>
 * 直接使用 dify-java-client 的原始响应类型，确保响应格式与 Dify 官方 API 完全一致。
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyChatServiceImpl implements DifyChatService {

    private final DifyChatflowClient difyChatflowClient;
    private final DifyConversationDAO difyConversationDAO;

    @Override
    public ChatMessageResponse sendMessage(String userUuid, UserType userType, DifyChatVO getData) {
        log.info("发送 Dify 消息 - userUuid: {}, userType: {}, conversationId: {}", userUuid, userType, getData.getConversationId());

        try {
            // 构建聊天消息请求
            ChatMessage.ChatMessageBuilder builder = ChatMessage.builder()
                    .query(getData.getQuery())
                    .user(userUuid);

            // 如果前端没有传 conversationId，则从数据库查询最新的
            String conversationId = getData.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                DifyConversationDO latestConv = difyConversationDAO.getLatestConversation(userUuid, userType.name());
                if (latestConv != null) {
                    conversationId = latestConv.getDifyConversationId();
                    log.info("从数据库获取最新会话ID: {}", conversationId);
                }
            }

            // 如果有会话ID，则继续该会话
            if (conversationId != null && !conversationId.isEmpty()) {
                builder.conversationId(conversationId);
            }

            // 如果有学期UUID，则传入 inputs 变量
            if (getData.getSemesterUuid() != null && !getData.getSemesterUuid().isEmpty()) {
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("semester_uuid", getData.getSemesterUuid());
                builder.inputs(inputs);
            }

            ChatMessage chatMessage = builder.build();

            // 使用阻塞模式发送消息，直接返回 Dify 官方响应
            ChatMessageResponse response = difyChatflowClient.sendChatMessage(chatMessage);

            // 保存或更新会话记录
            saveOrUpdateConversation(userUuid, userType.name(), response.getConversationId());

            return response;

        } catch (IOException | DifyApiException e) {
            log.error("Dify API 调用失败 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public SseEmitter sendMessageStream(String userUuid, UserType userType, DifyChatVO getData) {
        log.info("流式发送 Dify 消息 - userUuid: {}, userType: {}, conversationId: {}, semesterUuid: {}",
                userUuid, userType, getData.getConversationId(), getData.getSemesterUuid());

        // 创建 SSE Emitter，设置 5 分钟超时
        SseEmitter emitter = new SseEmitter(300000L);

        // 生成唯一的 taskId，用于整个会话的跟踪
        String taskId = UUID.randomUUID().toString();

        // 构建聊天消息请求
        ChatMessage.ChatMessageBuilder builder = ChatMessage.builder()
                .query(getData.getQuery())
                .user(userUuid);

        // 如果前端没有传 conversationId，则从数据库查询最新的
        String conversationId = getData.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            DifyConversationDO latestConv = difyConversationDAO.getLatestConversation(userUuid, userType.name());
            if (latestConv != null) {
                conversationId = latestConv.getDifyConversationId();
                log.info("从数据库获取最新会话ID: {}", conversationId);
            }
        }

        // 如果有会话ID，则继续该会话
        if (conversationId != null && !conversationId.isEmpty()) {
            builder.conversationId(conversationId);
        }

        // 如果有学期UUID，则传入 inputs 变量
        if (getData.getSemesterUuid() != null && !getData.getSemesterUuid().isEmpty()) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("semester_uuid", getData.getSemesterUuid());
            builder.inputs(inputs);
            log.info("设置 semester_uuid 到 inputs: {}", getData.getSemesterUuid());
        } else {
            log.warn("semesterUuid 为空，未传递给 Dify");
        }

        // 用于在回调中保存 conversationId
        final String finalUserUuid = userUuid;
        final String finalUserType = userType.name();
        final String[] capturedConversationId = {conversationId};

        ChatMessage chatMessage = builder.build();

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时 - userUuid: {}, taskId: {}", userUuid, taskId);
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接完成 - userUuid: {}, taskId: {}", userUuid, taskId));
        emitter.onError(throwable -> log.error("SSE 连接错误 - userUuid: {}, taskId: {}, error: {}",
                userUuid, taskId, throwable.getMessage()));

        // 使用流式模式发送消息
        try {
            difyChatflowClient.sendChatMessageStream(chatMessage, new ChatflowStreamCallback() {
                // 防止重复 complete 的标志
                final AtomicBoolean completed = new AtomicBoolean(false);

                @Override
                public void onWorkflowStarted(WorkflowStartedEvent event) {
                    log.debug("工作流开始 - taskId: {}, workflowRunId: {}", taskId, event.getWorkflowRunId());
                    try {
                        // 设置统一的 taskId
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("workflow_started")
                                .data(event));
                    } catch (IOException e) {
                        log.error("发送 workflow_started 事件失败 - taskId: {}", taskId, e);
                    }
                }

                @Override
                public void onNodeStarted(NodeStartedEvent event) {
                    NodeStartedEvent.NodeStartedData nodeData = event.getData();
                    log.debug("节点开始 - taskId: {}, node: {}, title: {}",
                            taskId, nodeData != null ? nodeData.getNodeId() : null,
                            nodeData != null ? nodeData.getTitle() : null);
                    try {
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("node_started")
                                .data(event));
                    } catch (IOException e) {
                        log.error("发送 node_started 事件失败 - taskId: {}", taskId, e);
                    }
                }

                @Override
                public void onNodeFinished(NodeFinishedEvent event) {
                    NodeFinishedEvent.NodeFinishedData nodeData = event.getData();
                    log.debug("节点结束 - taskId: {}, node: {}, status: {}",
                            taskId, nodeData != null ? nodeData.getNodeId() : null,
                            nodeData != null ? nodeData.getStatus() : null);
                    try {
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("node_finished")
                                .data(event));
                    } catch (IOException e) {
                        log.error("发送 node_finished 事件失败 - taskId: {}", taskId, e);
                    }
                }

                @Override
                public void onMessage(MessageEvent event) {
                    log.debug("收到消息片段 - taskId: {}, answer: {}", taskId, event.getAnswer());
                    try {
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(event));
                    } catch (IOException e) {
                        log.error("发送 message 事件失败 - taskId: {}", taskId, e);
                    }
                }

                @Override
                public void onMessageEnd(MessageEndEvent event) {
                    log.info("消息完成 - taskId: {}, messageId: {}, conversationId: {}",
                            taskId, event.getMessageId(), event.getConversationId());
                    // 捕获 conversationId 用于保存
                    if (event.getConversationId() != null) {
                        capturedConversationId[0] = event.getConversationId();
                    }
                    completeIfNeeded(event, null);
                }

                @Override
                public void onWorkflowFinished(WorkflowFinishedEvent event) {
                    log.info("工作流完成 - taskId: {}, status: {}",
                            taskId, event.getData() != null ? event.getData().getStatus() : "unknown");
                    // Chatflow 在某些情况下只触发 workflow_finished 而不触发 message_end
                    completeIfNeeded(null, event);
                }

                /**
                 * 统一处理消息结束和工作流结束，防止重复发送
                 */
                private void completeIfNeeded(MessageEndEvent messageEndEvent, WorkflowFinishedEvent workflowFinishedEvent) {
                    // 使用 CAS 操作确保只执行一次
                    if (!completed.compareAndSet(false, true)) {
                        log.debug("已完成，跳过重复调用 - taskId: {}", taskId);
                        return;
                    }

                    try {
                        // 优先使用 message_end 事件，如果没有则使用 workflow_finished 事件
                        if (messageEndEvent != null) {
                            messageEndEvent.setTaskId(taskId);
                            emitter.send(SseEmitter.event()
                                    .name("message_end")
                                    .data(messageEndEvent));
                        } else if (workflowFinishedEvent != null) {
                            workflowFinishedEvent.setTaskId(taskId);
                            emitter.send(SseEmitter.event()
                                    .name("workflow_finished")
                                    .data(workflowFinishedEvent));
                        }

                        // 保存或更新会话记录
                        if (capturedConversationId[0] != null && !capturedConversationId[0].isEmpty()) {
                            saveOrUpdateConversation(finalUserUuid, finalUserType, capturedConversationId[0]);
                        }

                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送结束事件失败 - taskId: {}", taskId, e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onPing(PingEvent event) {
                    log.debug("收到心跳 - taskId: {}", taskId);
                    try {
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("ping")
                                .data(event));
                    } catch (IOException e) {
                        log.error("发送 ping 事件失败 - taskId: {}", taskId, e);
                    }
                }

                @Override
                public void onError(ErrorEvent event) {
                    log.error("Dify 流式响应错误 - taskId: {}, code: {}, message: {}",
                            taskId, event.getCode(), event.getMessage());
                    try {
                        event.setTaskId(taskId);
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(event));
                        emitter.completeWithError(new RuntimeException(event.getMessage()));
                    } catch (IOException e) {
                        log.error("发送 error 事件失败 - taskId: {}", taskId, e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("Dify 流式响应异常 - taskId: {}", taskId, throwable);
                    emitter.completeWithError(throwable);
                }
            });
        } catch (Exception e) {
            log.error("启动流式消息失败 - userUuid: {}, taskId: {}, error: {}", userUuid, taskId, e.getMessage(), e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    public List<Conversation> getConversations(String userUuid) {
        log.info("获取 Dify 会话列表 - userUuid: {}", userUuid);

        try {
            // 使用正确的 API 签名: getConversations(user, lastId, limit, sortBy)
            ConversationListResponse response = difyChatflowClient.getConversations(userUuid, null, 20, "-updated_at");

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            // 直接返回 Dify 官方格式的 Conversation 列表
            return response.getData();

        } catch (IOException | DifyApiException e) {
            log.error("获取 Dify 会话列表失败 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new BusinessException("获取会话列表失败", ErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    public List<MessageListResponse.Message> getMessages(String userUuid, String conversationId) {
        log.info("获取 Dify 会话消息 - userUuid: {}, conversationId: {}", userUuid, conversationId);

        try {
            // 使用正确的 API 签名: getMessages(conversationId, user, firstId, limit)
            MessageListResponse response = difyChatflowClient.getMessages(conversationId, userUuid, null, 100);

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            // 直接返回 Dify 官方格式的 Message 列表
            return response.getData();

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
     * 保存或更新会话记录
     *
     * @param userUuid          用户UUID
     * @param userType          用户类型
     * @param difyConversationId Dify会话ID
     */
    private void saveOrUpdateConversation(String userUuid, String userType, String difyConversationId) {
        if (difyConversationId == null || difyConversationId.isEmpty()) {
            log.warn("difyConversationId 为空，跳过保存");
            return;
        }

        try {
            // 查找是否已存在该会话
            DifyConversationDO existing = difyConversationDAO.getByUserAndDifyConversationId(userUuid, userType, difyConversationId);

            if (existing != null) {
                // 已存在：更新时间戳
                existing.setUpdatedAt(LocalDateTime.now());
                difyConversationDAO.updateById(existing);
                log.info("更新会话记录 - userUuid: {}, conversationId: {}", userUuid, difyConversationId);
            } else {
                // 新会话：插入记录
                DifyConversationDO newConv = new DifyConversationDO()
                        .setConversationUuid(UuidUtil.generateUuidNoDash())
                        .setUserUuid(userUuid)
                        .setUserType(userType)
                        .setDifyConversationId(difyConversationId)
                        .setCreatedAt(LocalDateTime.now())
                        .setUpdatedAt(LocalDateTime.now());
                difyConversationDAO.save(newConv);
                log.info("保存新会话记录 - userUuid: {}, conversationId: {}", userUuid, difyConversationId);
            }
        } catch (Exception e) {
            // 保存失败不影响主流程，只记录日志
            log.error("保存会话记录失败 - userUuid: {}, conversationId: {}, error: {}",
                    userUuid, difyConversationId, e.getMessage(), e);
        }
    }
}
