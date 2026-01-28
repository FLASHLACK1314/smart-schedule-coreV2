package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.BuildingInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 教学楼控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/building")
public class BuildingController {
    private final BuildingService buildingService;

    /**
     * 新增教学楼
     *
     * @param token        Token
     * @param buildingNum  教学楼编号
     * @param buildingName 教学楼名称
     * @return 添加教学楼成功的信息
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> addBuilding(
            @RequestHeader("Authorization") String token,
            @RequestParam("building_num") String buildingNum,
            @RequestParam("building_name") String buildingName
    ) {
        buildingService.addBuilding(buildingNum, buildingName);
        return ResultUtil.success("添加教学楼成功");
    }

    /**
     * 教学楼信息分页查询
     *
     * @param page         页码
     * @param size         每页数量
     * @param buildingNum  教学楼编号（可选，用于模糊查询）
     * @param buildingName 教学楼名称（可选，用于模糊查询）
     * @return 教学楼信息列表（PageDTO<BuildingInfoDTO>）
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<PageDTO<BuildingInfoDTO>>> getBuildingPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "building_num", required = false) String buildingNum,
            @RequestParam(value = "building_name", required = false) String buildingName
    ) {
        PageDTO<BuildingInfoDTO> result = buildingService.getBuildingPage(page, size, buildingNum, buildingName);
        return ResultUtil.success("获取教学楼信息成功", result);
    }

    /**
     * 更新教学楼信息
     *
     * @param token        Token
     * @param buildingUuid 教学楼uuid
     * @param buildingNum  教学楼编号
     * @param buildingName 教学楼名称
     * @return 更新教学楼信息成功信息
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> updateBuilding(
            @RequestHeader("Authorization") String token,
            @RequestParam("building_uuid") String buildingUuid,
            @RequestParam("building_num") String buildingNum,
            @RequestParam("building_name") String buildingName
    ) {
        buildingService.updateBuilding(buildingUuid, buildingNum, buildingName);
        return ResultUtil.success("更新教学楼成功");
    }

    /**
     * 获取教学楼信息
     *
     * @param token        Token
     * @param buildingUuid 教学楼uuid
     * @return 教学楼信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<BuildingInfoDTO>> getBuilding(
            @RequestHeader("Authorization") String token,
            @RequestParam("building_uuid") String buildingUuid
    ) {
        BuildingInfoDTO result = buildingService.getBuilding(buildingUuid);
        return ResultUtil.success("获取教学楼信息成功", result);
    }

    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteBuilding(
            @RequestHeader("Authorization") String token,
            @RequestParam("building_uuid") String buildingUuid
    ) {
        buildingService.deleteBuilding(buildingUuid);
        return ResultUtil.success("删除教学楼成功");
    }


}
