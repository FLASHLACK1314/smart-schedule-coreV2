package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddTeacherVO;
import io.github.flashlack1314.smartschedulecorev2.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教师控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/teacher")
public class TeacherController {
    private final TeacherService teacherService;

    /**
     * 添加教师
     *
     * @param token   Token
     * @param getData 添加教师信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addTeacher(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTeacherVO getData
    ) {
        teacherService.addTeacher(
                getData.getTeacherNum(),
                getData.getTeacherName(),
                getData.getTitle(),
                getData.getDepartmentUuid(),
                getData.getTeacherPassword(),
                getData.getMaxHoursPerWeek(),
                getData.getLikeTime(),
                getData.getIsActive()
        );
        return ResultUtil.success("添加教师成功");
    }

    /**
     * 获取教师分页信息
     *
     * @param token          Token
     * @param page           页码
     * @param size           每页数量
     * @param teacherName    教师姓名（可选，模糊查询）
     * @param teacherNum     教师工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 教师分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<TeacherInfoDTO>>> getTeacherPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String teacherNum,
            @RequestParam(required = false) String departmentUuid
    ) {
        PageDTO<TeacherInfoDTO> result = teacherService.getTeacherPage(page, size, teacherName, teacherNum, departmentUuid);
        return ResultUtil.success("获取教师分页信息成功", result);
    }

    /**
     * 更新教师信息
     *
     * @param token   Token
     * @param getData 更新教师信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateTeacher(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTeacherVO getData
    ) {
        teacherService.updateTeacher(
                getData.getTeacherUuid(),
                getData.getTeacherNum(),
                getData.getTeacherName(),
                getData.getTitle(),
                getData.getDepartmentUuid(),
                getData.getTeacherPassword(),
                getData.getMaxHoursPerWeek(),
                getData.getLikeTime(),
                getData.getIsActive()
        );
        return ResultUtil.success("更新教师成功");
    }

    /**
     * 获取单个教师信息
     *
     * @param token       Token
     * @param teacherUuid 教师UUID
     * @return 单个教师信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<TeacherInfoDTO>> getTeacher(
            @RequestHeader("Authorization") String token,
            @RequestParam String teacherUuid
    ) {
        TeacherInfoDTO result = teacherService.getTeacher(teacherUuid);
        return ResultUtil.success("获取教师信息成功", result);
    }

    /**
     * 删除教师
     *
     * @param token       Token
     * @param teacherUuid 教师UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteTeacher(
            @RequestHeader("Authorization") String token,
            @RequestParam String teacherUuid
    ) {
        teacherService.deleteTeacher(teacherUuid);
        return ResultUtil.success("删除教师成功");
    }
}
