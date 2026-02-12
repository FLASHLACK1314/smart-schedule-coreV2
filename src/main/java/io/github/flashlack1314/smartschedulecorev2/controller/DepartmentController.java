package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.DepartmentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddDepartmentVO;
import io.github.flashlack1314.smartschedulecorev2.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 学院控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/department")
public class  DepartmentController {
    private final DepartmentService departmentService;

    /**
     * 添加学院
     *
     * @param token   Token
     * @param getData 添加学院信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addDepartment(
            @RequestHeader("Authorization") String token,
            @RequestBody AddDepartmentVO getData
    ) {
        departmentService.addDepartment(getData.getDepartmentName());
        return ResultUtil.success("添加学院成功");
    }

    /**
     * 获取学院分页信息
     *
     * @param token          Token
     * @param page           页码
     * @param size           每页数量
     * @param departmentName 学院名称（可选，模糊查询）
     * @return 学院分页信息
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<DepartmentInfoDTO>>> getDepartmentPage(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(value = "department_name", required = false) String departmentName
    ) {
        PageDTO<DepartmentInfoDTO> result = departmentService.getDepartmentPage(page, size, departmentName);
        return ResultUtil.success("获取学院分页信息成功", result);
    }

    /**
     * 更新学院信息
     *
     * @param token   Token
     * @param getData 更新学院信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateDepartment(
            @RequestHeader("Authorization") String token,
            @RequestBody AddDepartmentVO getData
    ) {
        departmentService.updateDepartment(getData.getDepartmentUuid(), getData.getDepartmentName());
        return ResultUtil.success("更新学院成功");
    }

    /**
     * 获取单个学院信息
     *
     * @param token          Token
     * @param departmentUuid 学院UUID
     * @return 单个学院信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<DepartmentInfoDTO>> getDepartment(
            @RequestHeader("Authorization") String token,
            @RequestParam("department_uuid") String departmentUuid
    ) {
        DepartmentInfoDTO result = departmentService.getDepartment(departmentUuid);
        return ResultUtil.success("获取学院信息成功", result);
    }

    /**
     * 删除学院
     *
     * @param token          Token
     * @param departmentUuid 学院UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteDepartment(
            @RequestHeader("Authorization") String token,
            @RequestParam("department_uuid") String departmentUuid
    ) {
        departmentService.deleteDepartment(departmentUuid);
        return ResultUtil.success("删除学院成功");
    }
}
