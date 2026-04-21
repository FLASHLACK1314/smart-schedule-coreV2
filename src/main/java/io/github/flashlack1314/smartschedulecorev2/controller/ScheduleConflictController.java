package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleConflictInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.ScheduleConflictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 排课冲突控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/scheduleConflict")
public class ScheduleConflictController {
    private final ScheduleConflictService scheduleConflictService;

    /**
     * 分页查询排课冲突
     *
     * @param token         Token
     * @param page          页码
     * @param size          每页数量
     * @param semesterUuid  学期UUID（可选）
     * @param severity      严重程度：1-硬冲突, 0-软冲突（可选）
     * @param conflictType  冲突类型（可选）
     * @return 分页结果
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<PageDTO<ScheduleConflictInfoDTO>>> getConflictPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid,
            @RequestParam(value = "severity", required = false) Integer severity,
            @RequestParam(value = "conflict_type", required = false) String conflictType
    ) {
        PageDTO<ScheduleConflictInfoDTO> result = scheduleConflictService.getConflictPage(
                page, size, semesterUuid, severity, conflictType);
        return ResultUtil.success("获取排课冲突分页信息成功", result);
    }

    /**
     * 获取冲突详情
     *
     * @param token         Token
     * @param conflictUuid  冲突UUID
     * @return 冲突详情
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<ScheduleConflictInfoDTO>> getConflict(
            @RequestHeader("Authorization") String token,
            @RequestParam("conflict_uuid") String conflictUuid
    ) {
        ScheduleConflictInfoDTO result = scheduleConflictService.getConflict(conflictUuid);
        return ResultUtil.success("获取冲突详情成功", result);
    }

    /**
     * 获取冲突统计信息
     *
     * @param token         Token
     * @param semesterUuid  学期UUID（可选）
     * @return 统计信息
     */
    @GetMapping("/stats")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Map<String, Object>>> getConflictStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid
    ) {
        Map<String, Object> result = scheduleConflictService.getConflictStats(semesterUuid);
        return ResultUtil.success("获取冲突统计信息成功", result);
    }

    /**
     * 删除冲突记录
     *
     * @param token         Token
     * @param conflictUuid  冲突UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteConflict(
            @RequestHeader("Authorization") String token,
            @RequestParam("conflict_uuid") String conflictUuid
    ) {
        scheduleConflictService.deleteConflict(conflictUuid);
        return ResultUtil.success("删除冲突记录成功");
    }

    /**
     * 根据排课UUID查询相关冲突
     *
     * @param token         Token
     * @param scheduleUuid  排课UUID
     * @return 冲突列表
     */
    @GetMapping("/bySchedule")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<List<ScheduleConflictInfoDTO>>> getConflictsBySchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam("schedule_uuid") String scheduleUuid
    ) {
        List<ScheduleConflictInfoDTO> result = scheduleConflictService.getConflictsByScheduleUuid(scheduleUuid);
        return ResultUtil.success("获取排课冲突列表成功", result);
    }
}
