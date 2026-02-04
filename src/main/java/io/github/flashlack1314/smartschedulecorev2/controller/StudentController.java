package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddStudentVO;
import io.github.flashlack1314.smartschedulecorev2.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 学生控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/student")
public class StudentController {
    private final StudentService studentService;

    /**
     * 新增学生
     *
     * @param token   Token
     * @param getData 新增学生信息
     * @return 新增学生结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addStudent(
            @RequestHeader("Authorization") String token,
            @RequestBody AddStudentVO getData
    ) {
        studentService.addStudent(getData);
        return ResultUtil.success("添加学生成功");
    }
}
