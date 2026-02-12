package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseQualificationInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseQualificationVO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseQualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程教师资格关联控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/courseQualification")
public class CourseQualificationController {
    private final CourseQualificationService courseQualificationService;

    /**
     * 添加课程-教师资格关联
     *
     * @param token   Token
     * @param getData 添加关联信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addQualification(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseQualificationVO getData
    ) {
        courseQualificationService.addQualification(getData.getCourseUuid(), getData.getTeacherUuid());
        return ResultUtil.success("添加资格关联成功");
    }

    /**
     * 获取课程-教师资格关联分页信息
     *
     * @param token       Token
     * @param page        页码
     * @param size        每页数量
     * @param courseUuid  课程UUID（可选过滤）
     * @param teacherUuid 教师UUID（可选过滤）
     * @return 关联分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<CourseQualificationInfoDTO>>> getQualificationPage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(value = "course_uuid", required = false) String courseUuid,
            @RequestParam(value = "teacher_uuid", required = false) String teacherUuid
    ) {
        PageDTO<CourseQualificationInfoDTO> result = courseQualificationService.getQualificationPage(
                page, size, courseUuid, teacherUuid);
        return ResultUtil.success("获取资格关联分页信息成功", result);
    }

    /**
     * 删除课程-教师资格关联
     *
     * @param token             Token
     * @param qualificationUuid 关联UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteQualification(
            @RequestHeader("Authorization") String token,
            @RequestParam("qualification_uuid") String qualificationUuid
    ) {
        courseQualificationService.deleteQualification(qualificationUuid);
        return ResultUtil.success("删除资格关联成功");
    }
}
