package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddAcademicAdminVO;
import io.github.flashlack1314.smartschedulecorev2.service.AcademicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教务控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/academic")
public class AcademicController {
    private final AcademicService academicService;

    /**
     * 添加教务管理员
     *
     * @param token   Token
     * @param getData 添加教务管理员信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole(UserType.SYSTEM_ADMIN)
    public ResponseEntity<BaseResponse<Void>> addAcademicAdmin(
            @RequestHeader("Authorization") String token,
            @RequestBody AddAcademicAdminVO getData
    ) {
        academicService.addAcademicAdmin(
                getData.getAcademicNum(),
                getData.getAcademicName(),
                getData.getDepartmentUuid(),
                getData.getAcademicPassword()
        );
        return ResultUtil.success("添加教务管理员成功");
    }

    /**
     * 获取教务管理员分页信息
     *
     * @param token          Token
     * @param page           页码
     * @param size           每页数量
     * @param academicName   教务名称（可选，模糊查询）
     * @param academicNum    教务工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 教务管理员分页信息
     */
    @GetMapping("/getPage")
    @RequireRole(UserType.SYSTEM_ADMIN)
    public ResponseEntity<BaseResponse<PageDTO<AcademicAdminInfoDTO>>> getAcademicAdminPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "academic_name", required = false) String academicName,
            @RequestParam(value = "academic_num", required = false) String academicNum,
            @RequestParam(value = "department_uuid", required = false) String departmentUuid
    ) {
        PageDTO<AcademicAdminInfoDTO> result = academicService.getAcademicAdminPage(page, size, academicName, academicNum, departmentUuid);
        return ResultUtil.success("获取教务管理员分页信息成功", result);
    }

    /**
     * 更新教务管理员信息
     *
     * @param token   Token
     * @param getData 更新教务管理员信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole(UserType.SYSTEM_ADMIN)
    public ResponseEntity<BaseResponse<Void>> updateAcademicAdmin(
            @RequestHeader("Authorization") String token,
            @RequestBody AddAcademicAdminVO getData
    ) {
        academicService.updateAcademicAdmin(
                getData.getAcademicUuid(),
                getData.getAcademicNum(),
                getData.getAcademicName(),
                getData.getDepartmentUuid(),
                getData.getAcademicPassword()
        );
        return ResultUtil.success("更新教务管理员成功");
    }

    /**
     * 获取单个教务管理员信息
     *
     * @param token        Token
     * @param academicUuid 教务UUID
     * @return 单个教务管理员信息
     */
    @GetMapping("/get")
    @RequireRole(UserType.SYSTEM_ADMIN)
    public ResponseEntity<BaseResponse<AcademicAdminInfoDTO>> getAcademicAdmin(
            @RequestHeader("Authorization") String token,
            @RequestParam("academic_uuid") String academicUuid
    ) {
        AcademicAdminInfoDTO result = academicService.getAcademicAdmin(academicUuid);
        return ResultUtil.success("获取教务管理员信息成功", result);
    }

    /**
     * 删除教务管理员
     *
     * @param token        Token
     * @param academicUuid 教务UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole(UserType.SYSTEM_ADMIN)
    public ResponseEntity<BaseResponse<Void>> deleteAcademicAdmin(
            @RequestHeader("Authorization") String token,
            @RequestParam("academic_uuid") String academicUuid
    ) {
        academicService.deleteAcademicAdmin(academicUuid);
        return ResultUtil.success("删除教务管理员成功");
    }
}
