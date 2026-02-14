package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScheduleVO;
import io.github.flashlack1314.smartschedulecorev2.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排课控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    // ===== 基础CRUD =====

    /**
     * 添加排课
     *
     * @param token   Token
     * @param getData 排课信息
     * @return 排课UUID
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<String>> addSchedule(
            @RequestHeader("Authorization") String token,
            @RequestBody AddScheduleVO getData
    ) {
        String scheduleUuid = scheduleService.addSchedule(getData);
        return ResultUtil.success("添加排课成功", scheduleUuid);
    }

    /**
     * 分页查询排课
     *
     * @param token             Token
     * @param page              页码
     * @param size              每页数量
     * @param semesterUuid      学期UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param classroomUuid     教室UUID（可选）
     * @param teacherUuid       教师UUID（可选）
     * @param dayOfWeek         星期几（可选）
     * @param status            状态（可选）
     * @return 分页结果
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<ScheduleInfoDTO>>> getSchedulePage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid,
            @RequestParam(value = "teaching_class_uuid", required = false) String teachingClassUuid,
            @RequestParam(value = "classroom_uuid", required = false) String classroomUuid,
            @RequestParam(value = "teacher_uuid", required = false) String teacherUuid,
            @RequestParam(value = "day_of_week", required = false) Integer dayOfWeek,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        PageDTO<ScheduleInfoDTO> result = scheduleService.getSchedulePage(
                page, size, semesterUuid, teachingClassUuid, classroomUuid,
                teacherUuid, dayOfWeek, status);
        return ResultUtil.success("获取排课分页信息成功", result);
    }

    /**
     * 获取排课详情
     *
     * @param token        Token
     * @param scheduleUuid 排课UUID
     * @return 排课详情
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<ScheduleInfoDTO>> getSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam("schedule_uuid") String scheduleUuid
    ) {
        ScheduleInfoDTO result = scheduleService.getSchedule(scheduleUuid);
        return ResultUtil.success("获取排课信息成功", result);
    }

    /**
     * 更新排课
     *
     * @param token   Token
     * @param getData 排课信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateSchedule(
            @RequestHeader("Authorization") String token,
            @RequestBody AddScheduleVO getData
    ) {
        scheduleService.updateSchedule(getData);
        return ResultUtil.success("更新排课信息成功");
    }

    /**
     * 删除排课
     *
     * @param token        Token
     * @param scheduleUuid 排课UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam("schedule_uuid") String scheduleUuid
    ) {
        scheduleService.deleteSchedule(scheduleUuid);
        return ResultUtil.success("删除排课成功");
    }

    // ===== 课表查询 =====

    /**
     * 获取教师课表
     *
     * @param token        Token
     * @param teacherUuid  教师UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    @GetMapping("/timetable/teacher")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<List<ScheduleInfoDTO>>> getTeacherTimetable(
            @RequestHeader("Authorization") String token,
            @RequestParam("teacher_uuid") String teacherUuid,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        List<ScheduleInfoDTO> result = scheduleService.getTeacherTimetable(teacherUuid, semesterUuid);
        return ResultUtil.success("获取教师课表成功", result);
    }

    /**
     * 获取学生课表
     *
     * @param token        Token
     * @param studentUuid  学生UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    @GetMapping("/timetable/student")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<List<ScheduleInfoDTO>>> getStudentTimetable(
            @RequestHeader("Authorization") String token,
            @RequestParam("student_uuid") String studentUuid,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        List<ScheduleInfoDTO> result = scheduleService.getStudentTimetable(studentUuid, semesterUuid);
        return ResultUtil.success("获取学生课表成功", result);
    }

    /**
     * 获取行政班课表
     *
     * @param token        Token
     * @param classUuid    行政班UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    @GetMapping("/timetable/class")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<List<ScheduleInfoDTO>>> getClassTimetable(
            @RequestHeader("Authorization") String token,
            @RequestParam("class_uuid") String classUuid,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        List<ScheduleInfoDTO> result = scheduleService.getClassTimetable(classUuid, semesterUuid);
        return ResultUtil.success("获取行政班课表成功", result);
    }

    /**
     * 获取教室课表
     *
     * @param token         Token
     * @param classroomUuid 教室UUID
     * @param semesterUuid  学期UUID
     * @return 课表列表
     */
    @GetMapping("/timetable/classroom")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<List<ScheduleInfoDTO>>> getClassroomTimetable(
            @RequestHeader("Authorization") String token,
            @RequestParam("classroom_uuid") String classroomUuid,
            @RequestParam("semester_uuid") String semesterUuid
    ) {
        List<ScheduleInfoDTO> result = scheduleService.getClassroomTimetable(classroomUuid, semesterUuid);
        return ResultUtil.success("获取教室课表成功", result);
    }
}
