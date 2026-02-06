package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.MajorInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 专业控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/major")
public class MajorController {
    private final MajorService majorService;

    /**
     * 新增专业
     *
     * @param token          Token
     * @param departmentUuid 学院UUID
     * @param majorNum       专业编号
     * @param majorName      专业名称
     * @return 添加专业成功的信息
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addMajor(
            @RequestHeader("Authorization") String token,
            @RequestParam("department_uuid") String departmentUuid,
            @RequestParam("major_num") String majorNum,
            @RequestParam("major_name") String majorName
    ) {
        majorService.addMajor(departmentUuid, majorNum, majorName);
        return ResultUtil.success("添加专业成功");
    }

    /**
     * 专业信息分页查询
     *
     * @param page           页码
     * @param size           每页数量
     * @param departmentUuid 学院UUID（可选，用于精确查询）
     * @param majorNum       专业编号（可选，用于模糊查询）
     * @param majorName      专业名称（可选，用于模糊查询）
     * @return 专业信息列表（PageDTO<MajorInfoDTO>）
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<MajorInfoDTO>>> getMajorPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "department_uuid", required = false) String departmentUuid,
            @RequestParam(value = "major_num", required = false) String majorNum,
            @RequestParam(value = "major_name", required = false) String majorName
    ) {
        PageDTO<MajorInfoDTO> result = majorService.getMajorPage(page, size, departmentUuid, majorNum, majorName);
        return ResultUtil.success("获取专业信息成功", result);
    }

    /**
     * 更新专业信息
     *
     * @param token          Token
     * @param majorUuid      专业UUID
     * @param departmentUuid 学院UUID
     * @param majorNum       专业编号
     * @param majorName      专业名称
     * @return 更新专业信息成功信息
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateMajor(
            @RequestHeader("Authorization") String token,
            @RequestParam("major_uuid") String majorUuid,
            @RequestParam("department_uuid") String departmentUuid,
            @RequestParam("major_num") String majorNum,
            @RequestParam("major_name") String majorName
    ) {
        majorService.updateMajor(majorUuid, departmentUuid, majorNum, majorName);
        return ResultUtil.success("更新专业成功");
    }

    /**
     * 获取单个专业信息
     *
     * @param token     Token
     * @param majorUuid 专业UUID
     * @return 单个专业信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<MajorInfoDTO>> getMajor(
            @RequestHeader("Authorization") String token,
            @RequestParam("major_uuid") String majorUuid
    ) {
        MajorInfoDTO result = majorService.getMajor(majorUuid);
        return ResultUtil.success("获取专业信息成功", result);
    }

    /**
     * 删除专业
     *
     * @param token     Token
     * @param majorUuid 专业UUID
     * @return 删除专业成功信息
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteMajor(
            @RequestHeader("Authorization") String token,
            @RequestParam("major_uuid") String majorUuid
    ) {
        majorService.deleteMajor(majorUuid);
        return ResultUtil.success("删除专业成功");
    }
}
