package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.BuildingInfoDTO;

/**
 * 教学楼服务
 *
 * @author flash
 */
public interface BuildingService {
    /**
     * 添加教学楼
     *
     * @param buildingNum  教学楼编号
     * @param buildingName 教学楼名称
     */
    void addBuilding(
            String buildingNum,
            String buildingName);

    /**
     * 获取教学楼信息页
     *
     * @param page         页码
     * @param size         每页数量
     * @param buildingNum  教学楼编号
     * @param buildingName 教学楼名称
     * @return 教学楼信息页
     */
    PageDTO<BuildingInfoDTO> getBuildingPage(
            int page,
            int size,
            String buildingNum,
            String buildingName);

    /**
     * 更新教学楼信息
     *
     * @param buildingUuid 教学楼UUID
     * @param buildingNum  教学楼编号
     * @param buildingName 教学楼名称
     */
    void updateBuilding(
            String buildingUuid,
            String buildingNum,
            String buildingName);

    /**
     * 获取教学楼信息
     *
     * @param buildingUuid 教学楼UUID
     * @return 教学楼信息
     */
    BuildingInfoDTO getBuilding(
            String buildingUuid);

    /**
     * 删除教学楼
     *
     * @param buildingUuid 教学楼UUID
     */
    void deleteBuilding(String buildingUuid);
}
