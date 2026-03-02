package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;
import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResultUtil.success("自动排课执行成功", result);
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
    public ResponseEntity<BaseResponse<Void>> confirmSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        log.info("确认排课方案，学期UUID: {}", semesterUuid);
        autoScheduleService.confirmSchedule(semesterUuid);
        return ResultUtil.success("确认排课方案成功");
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
