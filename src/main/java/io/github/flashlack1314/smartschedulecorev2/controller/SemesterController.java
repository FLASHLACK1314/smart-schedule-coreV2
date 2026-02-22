package io.github.flashlack1314.smartschedulecorev2.controller;


import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SemesterInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * 学期控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/semester")
public class SemesterController {
    private final SemesterService semesterService;

    /**
     * 新增学期
     *
     * @param token        Token
     * @param semesterName 学期名称
     * @param semesterWeeks 学期周数
     * @param startDate    学期开始日期
     * @param endDate      学期结束日期
     * @return 添加学期成功的信息
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addSemester(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_name") String semesterName,
            @RequestParam("semester_weeks") Integer semesterWeeks,
            @RequestParam("start_date") @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = ISO.DATE) LocalDate endDate
    ) {
        semesterService.addSemester(semesterName, semesterWeeks, startDate, endDate);
        return ResultUtil.success("添加学期成功");
    }

    /**
     * 学期信息分页查询
     *
     * @param page         页码
     * @param size         每页数量
     * @param semesterName 学期名称（可选，用于模糊查询）
     * @return 学期信息列表（PageDTO<SemesterInfoDTO>）
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<SemesterInfoDTO>>> getSemesterPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "semester_name", required = false) String semesterName
    ) {
        PageDTO<SemesterInfoDTO> result = semesterService.getSemesterPage(page, size, semesterName);
        return ResultUtil.success("获取学期信息成功", result);
    }

    /**
     * 更新学期信息
     *
     * @param token        Token
     * @param semesterUuid 学期uuid
     * @param semesterName 学期名称
     * @param semesterWeeks 学期周数
     * @param startDate    学期开始日期
     * @param endDate      学期结束日期
     * @return 更新学期信息成功信息
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateSemester(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid,
            @RequestParam("semester_name") String semesterName,
            @RequestParam("semester_weeks") Integer semesterWeeks,
            @RequestParam("start_date") @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = ISO.DATE) LocalDate endDate
    ) {
        semesterService.updateSemester(semesterUuid, semesterName, semesterWeeks, startDate, endDate);
        return ResultUtil.success("更新学期成功");
    }

    /**
     * 获取单个学期信息
     *
     * @param token        Token
     * @param semesterUuid 学期UUID
     * @return 单个学期信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<SemesterInfoDTO>> getSemester(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        SemesterInfoDTO result = semesterService.getSemester(semesterUuid);
        return ResultUtil.success("获取学期信息成功", result);
    }

    /**
     * 删除学期
     *
     * @param token        Token
     * @param semesterUuid 学期uuid
     * @return 删除学期成功信息
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteSemester(
            @RequestHeader("Authorization") String token,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        semesterService.deleteSemester(semesterUuid);
        return ResultUtil.success("删除学期成功");
    }
}
