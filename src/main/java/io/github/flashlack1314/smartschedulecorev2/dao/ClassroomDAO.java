package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
