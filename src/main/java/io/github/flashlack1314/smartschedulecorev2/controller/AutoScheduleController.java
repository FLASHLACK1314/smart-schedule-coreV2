package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;
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
@RequestMapping("/v1/auto-schedule")
public class AutoScheduleController {

    /**
     * 执行自动排课
     *
     * @param token   Token
     * @param request 排课请求参数
     * @return 排课结果
     */
    @PostMapping("/execute")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<AutoScheduleResult>> executeAutoSchedule(
            @RequestHeader("Authorization") String token,
            @RequestBody AutoScheduleVO request
    ) {
        // TODO: 实现自动排课逻辑
        return null;
    }

    /**
     * 保存排课方案为预览状态
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @return 保存结果
     */
    @PostMapping("/save-preview")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> saveAsPreview(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        // TODO: 实现保存预览逻辑
        return null;
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
        // TODO: 实现确认排课方案逻辑
        return null;
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
        // TODO: 实现清除预览逻辑
        return null;
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
        // TODO: 实现获取统计信息逻辑
        return null;
    }
}
