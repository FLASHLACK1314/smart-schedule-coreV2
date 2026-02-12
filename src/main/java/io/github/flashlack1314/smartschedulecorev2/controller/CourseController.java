package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseVO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/course")
public class CourseController {
    private final CourseService courseService;

    /**
     * 添加课程
     *
     * @param token      Token
     * @param addCourseVO 添加课程信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<String>> addCourse(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseVO addCourseVO
    ) {
        String courseUuid = courseService.addCourse(addCourseVO);
        return ResultUtil.success("添加课程成功", courseUuid);
    }

    /**
     * 获取课程分页信息
     *
     * @param page           页码
     * @param size           每页数量
     * @param courseName     课程名称（可选，模糊查询）
     * @param courseNum      课程编号（可选，模糊查询）
     * @param courseTypeUuid 课程类型UUID（可选）
     * @return 课程分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<CourseInfoDTO>>> getCoursePage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "course_name", required = false) String courseName,
            @RequestParam(value = "course_num", required = false) String courseNum,
            @RequestParam(value = "course_type_uuid", required = false) String courseTypeUuid
    ) {
        PageDTO<CourseInfoDTO> result = courseService.getCoursePage(page, size, courseName, courseNum, courseTypeUuid);
        return ResultUtil.success("获取课程分页信息成功", result);
    }

    /**
     * 更新课程信息
     *
     * @param token      Token
     * @param addCourseVO 更新课程信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateCourse(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseVO addCourseVO
    ) {
        courseService.updateCourse(addCourseVO);
        return ResultUtil.success("更新课程成功");
    }

    /**
     * 获取单个课程信息
     *
     * @param token      Token
     * @param courseUuid 课程UUID
     * @return 单个课程信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<CourseInfoDTO>> getCourse(
            @RequestHeader("Authorization") String token,
            @RequestParam("course_uuid") String courseUuid
    ) {
        CourseInfoDTO result = courseService.getCourse(courseUuid);
        return ResultUtil.success("获取课程信息成功", result);
    }

    /**
     * 删除课程
     *
     * @param token      Token
     * @param courseUuid 课程UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteCourse(
            @RequestHeader("Authorization") String token,
            @RequestParam("course_uuid") String courseUuid
    ) {
        courseService.deleteCourse(courseUuid);
        return ResultUtil.success("删除课程成功");
    }
}
