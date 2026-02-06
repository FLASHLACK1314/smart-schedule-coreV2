package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassVO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 行政班级控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/class")
public class ClassController {
    private final ClassService classService;

    /**
     * 添加行政班级
     *
     * @param token   Token
     * @param getData 添加行政班级信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addClass(
            @RequestHeader("Authorization") String token,
            @RequestBody AddClassVO getData
    ) {
        classService.addClass(getData.getMajorUuid(), getData.getClassName());
        return ResultUtil.success("添加行政班级成功");
    }

    /**
     * 获取行政班级分页信息
     *
     * @param token          Token
     * @param page           页码
     * @param size           每页数量
     * @param className      行政班级名称（可选，模糊查询）
     * @param majorUuid      专业UUID（可选）
     * @param departmentUuid 学院UUID（可选）
     * @return 行政班级分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<ClassInfoDTO>>> getClassPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String majorUuid,
            @RequestParam(required = false) String departmentUuid
    ) {
        PageDTO<ClassInfoDTO> result = classService.getClassPage(page, size, className, majorUuid, departmentUuid);
        return ResultUtil.success("获取行政班级分页信息成功", result);
    }

    /**
     * 更新行政班级信息
     *
     * @param token   Token
     * @param getData 更新行政班级信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateClass(
            @RequestHeader("Authorization") String token,
            @RequestBody AddClassVO getData
    ) {
        classService.updateClass(getData.getClassUuid(), getData.getMajorUuid(), getData.getClassName());
        return ResultUtil.success("更新行政班级成功");
    }

    /**
     * 获取单个行政班级信息
     *
     * @param token     Token
     * @param classUuid 行政班级UUID
     * @return 单个行政班级信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<ClassInfoDTO>> getClass(
            @RequestHeader("Authorization") String token,
            @RequestParam String classUuid
    ) {
        ClassInfoDTO result = classService.getClass(classUuid);
        return ResultUtil.success("获取行政班级信息成功", result);
    }

    /**
     * 删除行政班级
     *
     * @param token     Token
     * @param classUuid 行政班级UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteClass(
            @RequestHeader("Authorization") String token,
            @RequestParam String classUuid
    ) {
        classService.deleteClass(classUuid);
        return ResultUtil.success("删除行政班级成功");
    }
}
