package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddTeachingClassClassVO;
import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教学班-行政班关联控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/teachingClassClass")
public class TeachingClassClassController {
    private final TeachingClassClassService teachingClassClassService;

    /**
     * 添加行政班到教学班
     *
     * @param token   Token
     * @param getData 添加关联信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addTeachingClassClass(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTeachingClassClassVO getData
    ) {
        teachingClassClassService.addTeachingClassClass(getData.getTeachingClassUuid(), getData.getClassUuid());
        return ResultUtil.success("添加教学班-行政班关联成功");
    }

    /**
     * 获取教学班-行政班关联分页信息
     *
     * @param token              Token
     * @param page               页码
     * @param size               每页数量
     * @param teachingClassUuid  教学班UUID（可选过滤）
     * @param classUuid          行政班级UUID（可选过滤）
     * @return 关联分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<TeachingClassClassInfoDTO>>> getTeachingClassClassPage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(value = "teaching_class_uuid", required = false) String teachingClassUuid,
            @RequestParam(value = "class_uuid", required = false) String classUuid
    ) {
        PageDTO<TeachingClassClassInfoDTO> result = teachingClassClassService.getTeachingClassClassPage(
                page, size, teachingClassUuid, classUuid);
        return ResultUtil.success("获取教学班-行政班关联分页信息成功", result);
    }

    /**
     * 删除教学班-行政班关联
     *
     * @param token                   Token
     * @param teachingClassClassUuid  关联UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteTeachingClassClass(
            @RequestHeader("Authorization") String token,
            @RequestParam("teaching_class_class_uuid") String teachingClassClassUuid
    ) {
        teachingClassClassService.deleteTeachingClassClass(teachingClassClassUuid);
        return ResultUtil.success("删除教学班-行政班关联成功");
    }
}
