package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import io.github.flashlack1314.smartschedulecorev2.service.DifyChatService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import io.github.imfangs.dify.client.model.chat.Conversation;
import io.github.imfangs.dify.client.model.chat.MessageListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Dify 智能调课助手控制器
 * <p>
 * 提供与 Dify 工作流编排对话型应用的交互接口。
 * 仅限系统管理员和教务管理员访问。
 * <p>
 * 所有接口响应格式与 Dify 官方 API 完全一致，直接返回 dify-java-client 库的原始类型。
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dify/chat")
@RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
public class DifyChatController {

    private final DifyChatService difyChatService;
    private final TokenService tokenService;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 发送消息（创建或继续会话）
     * <p>
     * 返回 Dify 官方格式的 {@link ChatMessageResponse}，包含完整字段：
     * <ul>
     *   <li>message_id - 消息唯一标识</li>
     *   <li>conversation_id - 会话ID</li>
     *   <li>mode - 响应模式（chat/completion）</li>
     *   <li>answer - AI 回答内容</li>
     *   <li>metadata - 元数据（包含 usage.totalTokens 等信息）</li>
     *   <li>created_at - 创建时间戳（Unix 秒）</li>
     * </ul>
     *
     * @param authHeader Authorization Header
     * @param getData    聊天请求
     * @return Dify 官方格式的聊天响应
     */
    @PostMapping("/message")
    public ResponseEntity<BaseResponse<ChatMessageResponse>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DifyChatVO getData
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();
        UserType userType = tokenInfo.getUserType();

        log.info("Dify 聊天请求 - userUuid: {}, userType: {}, conversationId: {}", userUuid, userType, getData.getConversationId());
        ChatMessageResponse response = difyChatService.sendMessage(userUuid, userType, getData);
        return ResultUtil.success("消息发送成功", response);
    }

    /**
     * 流式发送消息（SSE）
     * <p>
     * 推荐用于涉及 MCP 工具调用等长时间操作的场景，避免超时。
     * 前端使用 EventSource 或 fetch + ReadableStream 接收。
     * <p>
     * 符合 Dify 官方 SSE 事件格式规范，事件类型：
     * <ul>
     *   <li>workflow_started: 工作流开始处理，包含 workflow_run_id、data.id、data.workflow_id 等</li>
     *   <li>node_started: 节点开始执行，包含 node_id、node_type、title、index 等</li>
     *   <li>node_finished: 节点执行结束，包含 status、outputs、error、elapsed_time 等</li>
     *   <li>message: 消息片段，包含 answer、message_id、conversation_id 等</li>
     *   <li>message_end: 消息完成，包含 metadata.usage、metadata.retriever_resources 等</li>
     *   <li>workflow_finished: 工作流执行结束，包含 status、outputs、elapsed_time、total_tokens 等</li>
     *   <li>ping: 心跳事件，保持连接活跃</li>
     *   <li>error: 错误事件，包含 status、code、message</li>
     * </ul>
     * <p>
     * 所有事件都包含 task_id 字段，用于请求跟踪。
     *
     * @param authHeader      Authorization Header
     * @param query           用户消息内容
     * @param conversationId  会话ID（可选，首次为空，后续对话传入之前返回的会话ID）
     * @param semesterUuid    学期UUID（可选，用于 MCP 工具调用时指定学期上下文）
     * @return SSE Emitter
     */
    @GetMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("query") String query,
            @RequestParam(value = "conversation_id", required = false) String conversationId,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid,
            @RequestParam(value = "force_new", required = false, defaultValue = "false") Boolean forceNew
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();
        UserType userType = tokenInfo.getUserType();
        log.info("Dify 流式聊天请求 - userUuid: {}, userType: {}, conversationId: {}, semesterUuid: {}, forceNew: {}", userUuid, userType, conversationId, semesterUuid, forceNew);
        DifyChatVO chatVO = new DifyChatVO();
        chatVO.setQuery(query);
        chatVO.setConversationId(conversationId);
        chatVO.setSemesterUuid(semesterUuid);
        chatVO.setForceNew(forceNew);
        return difyChatService.sendMessageStream(userUuid, userType, chatVO);
    }

    /**
     * 获取会话列表
     * <p>
     * 返回 Dify 官方格式的 {@link Conversation} 列表，每个会话包含：
     * <ul>
     *   <li>id - 会话ID</li>
     *   <li>name - 会话名称</li>
     *   <li>inputs - 输入参数</li>
     *   <li>status - 会话状态（normal/completed等）</li>
     *   <li>created_at - 创建时间戳（Unix 秒）</li>
     *   <li>updated_at - 更新时间戳（Unix 秒）</li>
     * </ul>
     *
     * @param authHeader Authorization Header
     * @return Dify 官方格式的会话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<BaseResponse<List<Conversation>>> getConversations(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("获取 Dify 会话列表 - userUuid: {}", userUuid);
        List<Conversation> conversations = difyChatService.getConversations(userUuid);
        return ResultUtil.success("获取会话列表成功", conversations);
    }

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
     *   <li>created_at - 创建时间戳（Unix 秒）</li>
     * </ul>
     *
     * @param authHeader      Authorization Header
     * @param conversationId  会话ID
     * @return Dify 官方格式的消息列表
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<BaseResponse<List<MessageListResponse.Message>>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("获取 Dify 会话消息 - userUuid: {}, conversationId: {}", userUuid, conversationId);
        List<MessageListResponse.Message> messages = difyChatService.getMessages(userUuid, conversationId);
        return ResultUtil.success("获取会话消息成功", messages);
    }

    /**
     * 删除会话
     *
     * @param authHeader      Authorization Header
     * @param conversationId  会话ID
     * @return 删除结果
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<BaseResponse<Void>> deleteConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("删除 Dify 会话 - userUuid: {}, conversationId: {}", userUuid, conversationId);
        difyChatService.deleteConversation(userUuid, conversationId);
        return ResultUtil.success("删除会话成功");
    }


    /**
     * 重命名会话
     *
     * @param authHeader      Authorization Header
     * @param conversationId  会话ID
     * @param name            新名称
     * @return 重命名结果
     */
    @PutMapping("/conversations/{conversationId}/name")
    public ResponseEntity<BaseResponse<Void>> renameConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId,
            @RequestParam("name") String name
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("重命名 Dify 会话 - userUuid: {}, conversationId: {}, name: {}", userUuid, conversationId, name);
        difyChatService.renameConversation(userUuid, conversationId, name);
        return ResultUtil.success("重命名会话成功");
    }

    /**
     * 清理 Token 的 Bearer 前缀
     *
     * @param authHeader Authorization Header
     * @return 纯 Token 字符串
     */
    private String cleanBearerPrefix(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return authHeader;
    }
}
