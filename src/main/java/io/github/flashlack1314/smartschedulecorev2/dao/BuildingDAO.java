package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.BuildingMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.BuildingDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教学楼DAO
 * @author flash
 */
@Slf4j
@Repository
public class BuildingDAO extends ServiceImpl<BuildingMapper, BuildingDO>
        implements IService<BuildingDO> {

    /**
     * 根据教学楼编号查询教学楼
     *
     * @param buildingNum 教学楼编号
     * @return 教学楼实体，如果不存在则返回null
     */
    public BuildingDO getByBuildingNum(String buildingNum) {
        LambdaQueryWrapper<BuildingDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BuildingDO::getBuildingNum, buildingNum);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查教学楼编号是否已存在
     *
     * @param buildingNum 教学楼编号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByBuildingNum(String buildingNum) {
        return getByBuildingNum(buildingNum) != null;
    }

    /**
     * 检查教学楼编号是否被其他教学楼使用（排除指定UUID的教学楼）
     *
     * @param buildingNum 教学楼编号
     * @param excludeUuid 要排除的教学楼UUID
     * @return 如果被其他教学楼使用返回true，否则返回false
     */
    public boolean existsByBuildingNumExcludeUuid(String buildingNum, String excludeUuid) {
        BuildingDO building = getByBuildingNum(buildingNum);
        return building != null && !building.getBuildingUuid().equals(excludeUuid);
    }

    /**
     * 检查教学楼UUID是否存在
     *
     * @param buildingUuid 教学楼UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByUuid(String buildingUuid) {
        return this.getById(buildingUuid) != null;
    }

    /**
     * 分页查询教学楼信息
     *
     * @param page         页码
     * @param size         每页数量
     * @param buildingNum  教学楼编号（可选，用于模糊查询）
     * @param buildingName 教学楼名称（可选，用于模糊查询）
     * @return 分页结果
     */
    public IPage<BuildingDO> getBuildingPage(int page, int size, String buildingNum, String buildingName) {
        log.debug("DAO层查询教学楼分页 - page: {}, size: {}, buildingNum: {}, buildingName: {}",
                page, size, buildingNum, buildingName);

        // 构建查询条件
        LambdaQueryWrapper<BuildingDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加模糊查询条件
        if (buildingNum != null && !buildingNum.trim().isEmpty()) {
            queryWrapper.like(BuildingDO::getBuildingNum, buildingNum);
        }
        if (buildingName != null && !buildingName.trim().isEmpty()) {
            queryWrapper.like(BuildingDO::getBuildingName, buildingName);
        }

        // 执行分页查询
        return this.page(new Page<>(page, size), queryWrapper);
    }
}
