package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseTypeVO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程类型控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/courseType")
public class CourseTypeController {
    private final CourseTypeService courseTypeService;

    /**
     * 添加课程类型
     *
     * @param token   Token
     * @param getData 添加课程类型信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addCourseType(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseTypeVO getData
    ) {
        courseTypeService.addCourseType(getData.getCourseTypeName());
        return ResultUtil.success("添加课程类型成功");
    }

    /**
     * 获取课程类型分页信息
     *
     * @param token          Token
     * @param page           页码
     * @param size           每页数量
     * @param courseTypeName 课程类型名称（可选，模糊查询）
     * @return 课程类型分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<CourseTypeInfoDTO>>> getCourseTypePage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String courseTypeName
    ) {
        PageDTO<CourseTypeInfoDTO> result = courseTypeService.getCourseTypePage(page, size, courseTypeName);
        return ResultUtil.success("获取课程类型分页信息成功", result);
    }

    /**
     * 更新课程类型信息
     *
     * @param token   Token
     * @param getData 更新课程类型信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateCourseType(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCourseTypeVO getData
    ) {
        courseTypeService.updateCourseType(getData.getCourseTypeUuid(), getData.getCourseTypeName());
        return ResultUtil.success("更新课程类型成功");
    }

    /**
     * 获取单个课程类型信息
     *
     * @param token          Token
     * @param courseTypeUuid 课程类型UUID
     * @return 单个课程类型信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<CourseTypeInfoDTO>> getCourseType(
            @RequestHeader("Authorization") String token,
            @RequestParam String courseTypeUuid
    ) {
        CourseTypeInfoDTO result = courseTypeService.getCourseType(courseTypeUuid);
        return ResultUtil.success("获取课程类型信息成功", result);
    }

    /**
     * 删除课程类型
     *
     * @param token          Token
     * @param courseTypeUuid 课程类型UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteCourseType(
            @RequestHeader("Authorization") String token,
            @RequestParam String courseTypeUuid
    ) {
        courseTypeService.deleteCourseType(courseTypeUuid);
        return ResultUtil.success("删除课程类型成功");
    }
}
