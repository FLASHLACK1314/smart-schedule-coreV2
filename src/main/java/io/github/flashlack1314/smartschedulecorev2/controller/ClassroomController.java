package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.ClassroomInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassroomVO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教室控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/classroom")
public class ClassroomController {
    private final ClassroomService classroomService;


    /**
     * 新增教室
     *
     * @param token   Token
     * @param getData 新增教室信息
     * @return 新增教室结果
     */
    @RequestMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addClassroom(
            @RequestHeader("Authorization") String token,
            @RequestBody AddClassroomVO getData

    ) {
        classroomService.addClassroom(getData);
        return ResultUtil.success("添加教室成功");
    }

    /**
     * 获取教室信息
     *
     * @param token         Token
     * @param classroomUuid 教室uuid
     * @return 教室信息
     */
    @RequestMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<ClassroomInfoDTO>> getClassroom(
            @RequestHeader("Authorization") String token,
            String classroomUuid
    ) {
        ClassroomInfoDTO result = classroomService.getClassroomInfo(classroomUuid);
        return ResultUtil.success("获取教室信息成功", result);
    }

}
