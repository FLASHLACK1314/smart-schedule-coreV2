package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyChatResponseDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyConversationDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.DifyMessageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.DifyChatVO;
import io.github.flashlack1314.smartschedulecorev2.service.DifyChatService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
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
 * 提供与 Dify 工作流编排对话型应用的交互接口
 * 仅限系统管理员和教务管理员访问
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
     *
     * @param authHeader Authorization Header
     * @param getData    聊天请求
     * @return 聊天响应
     */
    @PostMapping("/message")
    public ResponseEntity<BaseResponse<DifyChatResponseDTO>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DifyChatVO getData
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("Dify 聊天请求 - userUuid: {}, conversationId: {}", userUuid, getData.getConversationId());
        DifyChatResponseDTO response = difyChatService.sendMessage(userUuid, getData);
        return ResultUtil.success("消息发送成功", response);
    }

    /**
     * 流式发送消息（SSE）
     * <p>
     * 推荐用于涉及 MCP 工具调用等长时间操作的场景，避免超时。
     * 前端使用 EventSource 或 fetch + ReadableStream 接收。
     * <p>
     * 事件类型：
     * - workflow_started: 工作流开始处理
     * - node_started: 节点开始执行
     * - message: 消息片段
     * - done: 消息完成（包含完整响应和会话ID）
     * - error: 错误
     *
     * @param authHeader      Authorization Header
     * @param query           用户消息内容
     * @param conversationId  会话ID（可选，首次为空，后续对话传入之前返回的会话ID）
     * @return SSE Emitter
     */
    @GetMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("query") String query,
            @RequestParam(value = "conversationId", required = false) String conversationId
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("Dify 流式聊天请求 - userUuid: {}, conversationId: {}", userUuid, conversationId);

        DifyChatVO chatVO = new DifyChatVO();
        chatVO.setQuery(query);
        chatVO.setConversationId(conversationId);

        return difyChatService.sendMessageStream(userUuid, chatVO);
    }

    /**
     * 获取会话列表
     *
     * @param authHeader Authorization Header
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<BaseResponse<List<DifyConversationDTO>>> getConversations(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("获取 Dify 会话列表 - userUuid: {}", userUuid);
        List<DifyConversationDTO> conversations = difyChatService.getConversations(userUuid);
        return ResultUtil.success("获取会话列表成功", conversations);
    }

    /**
     * 获取会话历史消息
     *
     * @param authHeader      Authorization Header
     * @param conversationId  会话ID
     * @return 消息列表
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<BaseResponse<List<DifyMessageDTO>>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId
    ) {
        String token = cleanBearerPrefix(authHeader);
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();

        log.info("获取 Dify 会话消息 - userUuid: {}, conversationId: {}", userUuid, conversationId);
        List<DifyMessageDTO> messages = difyChatService.getMessages(userUuid, conversationId);
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
