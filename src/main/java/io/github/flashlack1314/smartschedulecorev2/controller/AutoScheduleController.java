package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.enums.ActionType;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;
import io.github.flashlack1314.smartschedulecorev2.service.ActivityLogService;
import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService;
import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService.ConfirmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 自动排课控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auto-schedule")
public class AutoScheduleController {

    private final AutoScheduleService autoScheduleService;
    private final ActivityLogService activityLogService;

    /**
     * 执行自动排课
     *
     * @param token          Token
     * @param autoScheduleVO 排课参数
     * @return 排课结果
     */
    @PostMapping("/execute")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<AutoScheduleResult>> executeAutoSchedule(
            @RequestHeader("Authorization") String token,
            @RequestBody AutoScheduleVO autoScheduleVO
    ) {
        log.info("执行自动排课，请求参数: {}", autoScheduleVO);
        AutoScheduleResult result = autoScheduleService.execute(autoScheduleVO);
        // 记录活动日志
        activityLogService.logActivity(token, ActionType.AUTO_SCHEDULE);
        return ResultUtil.success("自动排课执行成功", result);
    }

    /**
     * 执行自动排课（SSE版本，保持连接直到完成）
     *
     * @param token          Token
     * @param autoScheduleVO 排课参数
     * @return SSE流
     */
    @PostMapping(value = "/execute-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public SseEmitter executeAutoScheduleStream(
            @RequestHeader("Authorization") String token,
            @RequestBody AutoScheduleVO autoScheduleVO
    ) {
        log.info("SSE自动排课，请求参数: {}", autoScheduleVO);

        // 创建SSE emitter，超时时间10分钟
        SseEmitter emitter = new SseEmitter(600000L);

        // 异步执行排课
        new Thread(() -> {
            try {
                AutoScheduleResult result = autoScheduleService.executeWithSse(autoScheduleVO, emitter);
                // 发送最终结果
                emitter.send(SseEmitter.event()
                        .name("result")
                        .data(result));
            } catch (IllegalStateException e) {
                // SSE连接可能已关闭，忽略
                log.warn("SSE连接已关闭");
            } catch (Exception e) {
                log.error("SSE排课异常", e);
            } finally {
                try {
                    emitter.complete();
                } catch (IllegalStateException ignored) {
                    // 已经complete过了
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 保存排课方案为预览状态
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @param result       排课结果
     * @return 保存结果
     */
    @PostMapping("/save-preview")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> saveAsPreview(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid,
            @RequestBody AutoScheduleResult result
    ) {
        log.info("保存预览方案，学期UUID: {}", semesterUuid);
        autoScheduleService.saveAsPreview(semesterUuid, result);
        return ResultUtil.success("保存预览成功");
    }

    /**
     * 确认排课方案（将预览状态转为正式状态）
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @return 确认结果
     */
    @PostMapping("/confirm")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<ConfirmResult>> confirmSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        log.info("确认排课方案，学期UUID: {}", semesterUuid);
        ConfirmResult result = autoScheduleService.confirmSchedule(semesterUuid);
        return ResultUtil.success(result.getMessage(), result);
    }

    /**
     * 清除预览排课方案
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @return 清除结果
     */
    @DeleteMapping("/clear-preview")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> clearPreview(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        log.info("清除预览方案，学期UUID: {}", semesterUuid);
        autoScheduleService.clearPreview(semesterUuid);
        return ResultUtil.success("清除预览成功");
    }

    /**
     * 获取排课统计信息
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<AutoScheduleResult.ScheduleStatistics>> getStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        log.info("获取统计信息，学期UUID: {}", semesterUuid);
        AutoScheduleResult.ScheduleStatistics statistics = autoScheduleService.getStatistics(semesterUuid);
        return ResultUtil.success("获取统计信息成功", statistics);
    }
}
