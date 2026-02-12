package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseClassroomTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseClassroomTypeVO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseClassroomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程类型-教室类型关联控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/courseClassroomType")
public class CourseClassroomTypeController {
    private final CourseClassroomTypeService courseClassroomTypeService;

    /**
     * 添加课程类型-教室类型关联
     *
     * @param token Token
     * @param getData 添加关联信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addRelation(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseClassroomTypeVO getData
    ) {
        courseClassroomTypeService.addRelation(getData.getCourseTypeUuid(), getData.getClassroomTypeUuid());
        return ResultUtil.success("添加关联成功");
    }

    /**
     * 获取课程类型-教室类型关联分页信息
     *
     * @param token Token
     * @param page 页码
     * @param size 每页数量
     * @param courseTypeUuid 课程类型UUID（可选过滤）
     * @param classroomTypeUuid 教室类型UUID（可选过滤）
     * @return 关联分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<CourseClassroomTypeInfoDTO>>> getRelationPage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(value = "course_type_uuid", required = false) String courseTypeUuid,
            @RequestParam(value = "classroom_type_uuid", required = false) String classroomTypeUuid
    ) {
        PageDTO<CourseClassroomTypeInfoDTO> result = courseClassroomTypeService.getRelationPage(
                page, size, courseTypeUuid, classroomTypeUuid);
        return ResultUtil.success("获取关联分页信息成功", result);
    }

    /**
     * 删除课程类型-教室类型关联
     *
     * @param token Token
     * @param relationUuid 关联UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteRelation(
            @RequestHeader("Authorization") String token,
            @RequestParam("relation_uuid") String relationUuid
    ) {
        courseClassroomTypeService.deleteRelation(relationUuid);
        return ResultUtil.success("删除关联成功");
    }
}
