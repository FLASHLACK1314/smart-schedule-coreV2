package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.BuildingDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.BuildingInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.BuildingDO;
import io.github.flashlack1314.smartschedulecorev2.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教学楼服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingDAO buildingDAO;
    private final ClassroomDAO classroomDAO;

    @Override
    public void addBuilding(String buildingNum, String buildingName) {
        log.info("添加教学楼 - 编号: {}, 名称: {}", buildingNum, buildingName);

        // 检查教学楼编号是否已存在
        if (buildingDAO.existsByBuildingNum(buildingNum)) {
            throw new BusinessException("教学楼编号已存在: " + buildingNum, ErrorCode.OPERATION_FAILED);
        }

        // 创建教学楼对象
        BuildingDO buildingDO = new BuildingDO();
        // 生成32位UUID（去掉横线）
        buildingDO.setBuildingUuid(UuidUtil.generateUuidNoDash());
        buildingDO.setBuildingNum(buildingNum);
        buildingDO.setBuildingName(buildingName);
        // 保存到数据库
        boolean saved = buildingDAO.save(buildingDO);

        if (!saved) {
            throw new BusinessException("保存教学楼失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学楼添加成功 - UUID: {}, 编号: {}, 名称: {}",
                buildingDO.getBuildingUuid(), buildingNum, buildingName);
    }

    @Override
    public PageDTO<BuildingInfoDTO> getBuildingPage(int page, int size, String buildingNum, String buildingName) {
        log.info("查询教学楼分页信息 - page: {}, size: {}, buildingNum: {}, buildingName: {}",
                page, size, buildingNum, buildingName);

        // 调用DAO层进行分页查询
        IPage<BuildingDO> pageResult = buildingDAO.getBuildingPage(page, size, buildingNum, buildingName);

        // 转换为 BuildingInfoDTO
        List<BuildingInfoDTO> buildingInfoList = pageResult.getRecords().stream()
                .map(this::convertToBuildingInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<BuildingInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), buildingInfoList);

        log.info("查询教学楼分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateBuilding(String buildingUuid, String buildingNum, String buildingName) {
        log.info("更新教学楼信息 - UUID: {}, 编号: {}, 名称: {}", buildingUuid, buildingNum, buildingName);

        // 查询教学楼是否存在
        BuildingDO building = buildingDAO.getById(buildingUuid);
        if (building == null) {
            throw new BusinessException("教学楼不存在: " + buildingUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教学楼编号是否被其他教学楼使用
        if (buildingDAO.existsByBuildingNumExcludeUuid(buildingNum, buildingUuid)) {
            throw new BusinessException("教学楼编号已被其他教学楼使用: " + buildingNum, ErrorCode.OPERATION_FAILED);
        }

        // 更新教学楼信息
        building.setBuildingNum(buildingNum);
        building.setBuildingName(buildingName);

        // 保存更新
        boolean updated = buildingDAO.updateById(building);

        if (!updated) {
            throw new BusinessException("更新教学楼失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学楼更新成功 - UUID: {}, 编号: {}, 名称: {}", buildingUuid, buildingNum, buildingName);
    }

    @Override
    public BuildingInfoDTO getBuilding(String buildingUuid) {
        log.info("获取教学楼信息 - UUID: {}", buildingUuid);

        // 查询教学楼
        BuildingDO building = buildingDAO.getById(buildingUuid);

        if (building == null) {
            throw new BusinessException("教学楼不存在: " + buildingUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        BuildingInfoDTO buildingInfoDTO = convertToBuildingInfoDTO(building);

        log.info("获取教学楼信息成功 - UUID: {}, 编号: {}, 名称: {}",
                buildingUuid, building.getBuildingNum(), building.getBuildingName());

        return buildingInfoDTO;
    }

    @Override
    public void deleteBuilding(String buildingUuid) {
        log.info("删除教学楼 - UUID: {}", buildingUuid);

        // 查询教学楼是否存在
        BuildingDO building = buildingDAO.getById(buildingUuid);
        if (building == null) {
            throw new BusinessException("教学楼不存在: " + buildingUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教学楼下是否还有教室
        if (classroomDAO.existsByBuildingUuid(buildingUuid)) {
            long classroomCount = classroomDAO.countByBuildingUuid(buildingUuid);
            throw new BusinessException("该教学楼下还有 " + classroomCount + " 个教室，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = buildingDAO.removeById(buildingUuid);

        if (!deleted) {
            throw new BusinessException("删除教学楼失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学楼删除成功 - UUID: {}, 编号: {}, 名称: {}",
                buildingUuid, building.getBuildingNum(), building.getBuildingName());
    }

    /**
     * 转换 BuildingDO 为 BuildingInfoDTO
     *
     * @param buildingDO 教学楼实体
     * @return 教学楼信息DTO
     */
    private BuildingInfoDTO convertToBuildingInfoDTO(BuildingDO buildingDO) {
        BuildingInfoDTO dto = new BuildingInfoDTO();
        dto.setBuildingUuid(buildingDO.getBuildingUuid());
        dto.setBuildingNum(buildingDO.getBuildingNum());
        dto.setBuildingName(buildingDO.getBuildingName());
        return dto;
    }

    /**
     * 构建分页DTO
     *
     * @param page    页码
     * @param size    每页数量
     * @param total   总数
     * @param records 记录列表
     * @return 分页DTO
     */
    private PageDTO<BuildingInfoDTO> buildPageDTO(int page, int size, int total, List<BuildingInfoDTO> records) {
        PageDTO<BuildingInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
