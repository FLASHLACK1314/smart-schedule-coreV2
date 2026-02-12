package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddStudentVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.UpdateStudentVO;
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

    /**
     * 分页查询学生
     *
     * @param token         Token
     * @param page          页码
     * @param size          每页数量
     * @param studentName   学生姓名
     * @param studentId     学号
     * @param classUuid     班级UUID
     * @param majorUuid     专业UUID
     * @param departmentUuid 学院UUID
     * @return 分页结果
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<StudentInfoDTO>>> getStudentPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "student_name", required = false) String studentName,
            @RequestParam(value = "student_id", required = false) String studentId,
            @RequestParam(value = "class_uuid", required = false) String classUuid,
            @RequestParam(value = "major_uuid", required = false) String majorUuid,
            @RequestParam(value = "department_uuid", required = false) String departmentUuid
    ) {
        PageDTO<StudentInfoDTO> result = studentService.getStudentPage(page, size, studentName, studentId, classUuid, majorUuid, departmentUuid);
        return ResultUtil.success("获取学生分页信息成功", result);
    }

    /**
     * 获取学生信息
     *
     * @param token       Token
     * @param studentUuid 学生UUID
     * @return 学生信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<StudentInfoDTO>> getStudent(
            @RequestHeader("Authorization") String token,
            @RequestParam("student_uuid") String studentUuid
    ) {
        StudentInfoDTO result = studentService.getStudent(studentUuid);
        return ResultUtil.success("获取学生信息成功", result);
    }

    /**
     * 更新学生信息
     *
     * @param token   Token
     * @param getData 更新学生信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateStudent(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateStudentVO getData
    ) {
        studentService.updateStudent(getData.getStudentUuid(), getData.getStudentId(), getData.getStudentName(), getData.getClassUuid(), getData.getStudentPassword());
        return ResultUtil.success("更新学生信息成功");
    }

    /**
     * 删除学生
     *
     * @param token       Token
     * @param studentUuid 学生UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteStudent(
            @RequestHeader("Authorization") String token,
            @RequestParam("student_uuid") String studentUuid
    ) {
        studentService.deleteStudent(studentUuid);
        return ResultUtil.success("删除学生成功");
    }
}
