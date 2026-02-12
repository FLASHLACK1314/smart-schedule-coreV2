package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassroomTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassroomTypeVO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教室类型控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/classroomType")
public class ClassroomTypeController {
    private final ClassroomTypeService classroomTypeService;

    /**
     * 添加教室类型
     *
     * @param token   Token
     * @param getData 添加教室类型信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addClassroomType(
            @RequestHeader("Authorization") String token,
            @RequestBody AddClassroomTypeVO getData
    ) {
        classroomTypeService.addClassroomType(getData.getClassroomTypeName());
        return ResultUtil.success("添加教室类型成功");
    }

    /**
     * 获取教室类型分页信息
     *
     * @param token             Token
     * @param page              页码
     * @param size              每页数量
     * @param classroomTypeName 教室类型名称（可选，模糊查询）
     * @return 教室类型分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<ClassroomTypeInfoDTO>>> getClassroomTypePage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(value = "classroom_type_name", required = false) String classroomTypeName
    ) {
        PageDTO<ClassroomTypeInfoDTO> result = classroomTypeService.getClassroomTypePage(page, size, classroomTypeName);
        return ResultUtil.success("获取教室类型分页信息成功", result);
    }

    /**
     * 更新教室类型信息
     *
     * @param token   Token
     * @param getData 更新教室类型信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateClassroomType(
            @RequestHeader("Authorization") String token,
            @RequestBody AddClassroomTypeVO getData
    ) {
        classroomTypeService.updateClassroomType(getData.getClassroomTypeUuid(), getData.getClassroomTypeName());
        return ResultUtil.success("更新教室类型成功");
    }

    /**
     * 获取单个教室类型信息
     *
     * @param token             Token
     * @param classroomTypeUuid 教室类型UUID
     * @return 单个教室类型信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<ClassroomTypeInfoDTO>> getClassroomType(
            @RequestHeader("Authorization") String token,
            @RequestParam("classroom_type_uuid") String classroomTypeUuid
    ) {
        ClassroomTypeInfoDTO result = classroomTypeService.getClassroomType(classroomTypeUuid);
        return ResultUtil.success("获取教室类型信息成功", result);
    }

    /**
     * 删除教室类型
     *
     * @param token             Token
     * @param classroomTypeUuid 教室类型UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteClassroomType(
            @RequestHeader("Authorization") String token,
            @RequestParam("classroom_type_uuid") String classroomTypeUuid
    ) {
        classroomTypeService.deleteClassroomType(classroomTypeUuid);
        return ResultUtil.success("删除教室类型成功");
    }
}
