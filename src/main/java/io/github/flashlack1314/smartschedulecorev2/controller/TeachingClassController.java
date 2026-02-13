package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddTeachingClassVO;
import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教学班控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/teachingClass")
public class TeachingClassController {
    private final TeachingClassService teachingClassService;

    /**
     * 新增教学班
     *
     * @param token   Token
     * @param getData 新增教学班信息
     * @return 新增教学班结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<String>> addTeachingClass(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTeachingClassVO getData
    ) {
        String teachingClassUuid = teachingClassService.addTeachingClass(getData);
        return ResultUtil.success("添加教学班成功", teachingClassUuid);
    }

    /**
     * 分页查询教学班
     *
     * @param token         Token
     * @param page          页码
     * @param size          每页数量
     * @param courseUuid    课程UUID
     * @param teacherUuid   教师UUID
     * @param semesterUuid  学期UUID
     * @return 分页结果
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<TeachingClassInfoDTO>>> getTeachingClassPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "course_uuid", required = false) String courseUuid,
            @RequestParam(value = "teacher_uuid", required = false) String teacherUuid,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid
    ) {
        PageDTO<TeachingClassInfoDTO> result = teachingClassService.getTeachingClassPage(
                page, size, courseUuid, teacherUuid, semesterUuid);
        return ResultUtil.success("获取教学班分页信息成功", result);
    }

    /**
     * 获取教学班信息
     *
     * @param token             Token
     * @param teachingClassUuid 教学班UUID
     * @return 教学班信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<TeachingClassInfoDTO>> getTeachingClass(
            @RequestHeader("Authorization") String token,
            @RequestParam("teaching_class_uuid") String teachingClassUuid
    ) {
        TeachingClassInfoDTO result = teachingClassService.getTeachingClass(teachingClassUuid);
        return ResultUtil.success("获取教学班信息成功", result);
    }

    /**
     * 更新教学班信息
     *
     * @param token   Token
     * @param getData 更新教学班信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateTeachingClass(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTeachingClassVO getData
    ) {
        teachingClassService.updateTeachingClass(getData);
        return ResultUtil.success("更新教学班信息成功");
    }

    /**
     * 删除教学班
     *
     * @param token             Token
     * @param teachingClassUuid 教学班UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteTeachingClass(
            @RequestHeader("Authorization") String token,
            @RequestParam("teaching_class_uuid") String teachingClassUuid
    ) {
        teachingClassService.deleteTeachingClass(teachingClassUuid);
        return ResultUtil.success("删除教学班成功");
    }
}
