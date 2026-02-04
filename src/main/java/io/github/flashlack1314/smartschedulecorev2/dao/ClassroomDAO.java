package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassroomMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教室DAO
 * @author flash
 */
@Slf4j
@Repository
public class ClassroomDAO extends ServiceImpl<ClassroomMapper, ClassroomDO>
        implements IService<ClassroomDO> {

    /**
     * 检查教学楼下是否还有教室
     *
     * @param buildingUuid 教学楼UUID
     * @return 如果存在教室返回true，否则返回false
     */
    public boolean existsByBuildingUuid(String buildingUuid) {
        LambdaQueryWrapper<ClassroomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomDO::getBuildingUuid, buildingUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教学楼下的教室数量
     *
     * @param buildingUuid 教学楼UUID
     * @return 教室数量
     */
    public long countByBuildingUuid(String buildingUuid) {
        LambdaQueryWrapper<ClassroomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomDO::getBuildingUuid, buildingUuid);
        return this.count(queryWrapper);
    }

    /**
     * 分页查询教室信息
     *
     * @param page              页码
     * @param size              每页数量
     * @param buildingUuid      教学楼UUID（可选，用于精确查询）
     * @param classroomName     教室名称（可选，用于模糊查询）
     * @param classroomCapacity 教室容量（可选，用于精确查询）
     * @param classroomTypeUuid 教室类型UUID（可选，用于精确查询）
     * @return 分页结果
     */
    public IPage<ClassroomDO> getClassroomPage(int page, int size, String buildingUuid, String classroomName, String classroomCapacity, String classroomTypeUuid) {
        log.debug("DAO层查询教室分页 - page: {}, size: {}, buildingUuid: {}, classroomName: {}, classroomCapacity: {}, classroomTypeUuid: {}",
                page, size, buildingUuid, classroomName, classroomCapacity, classroomTypeUuid);

        // 构建查询条件
        LambdaQueryWrapper<ClassroomDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加精确查询条件
        if (buildingUuid != null && !buildingUuid.trim().isEmpty()) {
            queryWrapper.eq(ClassroomDO::getBuildingUuid, buildingUuid);
        }
        if (classroomTypeUuid != null && !classroomTypeUuid.trim().isEmpty()) {
            queryWrapper.eq(ClassroomDO::getClassroomTypeUuid, classroomTypeUuid);
        }
        if (classroomCapacity != null && !classroomCapacity.trim().isEmpty()) {
            queryWrapper.eq(ClassroomDO::getClassroomCapacity, classroomCapacity);
        }

        // 添加模糊查询条件
        if (classroomName != null && !classroomName.trim().isEmpty()) {
            queryWrapper.like(ClassroomDO::getClassroomName, classroomName);
        }

        // 执行分页查询
        return this.page(new Page<>(page, size), queryWrapper);
    }
}
