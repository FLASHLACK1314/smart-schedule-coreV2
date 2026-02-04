package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ScheduleMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ScheduleDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 排课DAO
 * @author flash
 */
@Slf4j
@Repository
public class ScheduleDAO extends ServiceImpl<ScheduleMapper, ScheduleDO>
        implements IService<ScheduleDO> {

    /**
     * 检查教室是否被排课使用
     *
     * @param classroomUuid 教室UUID
     * @return 如果被使用返回true，否则返回false
     */
    public boolean existsByClassroomUuid(String classroomUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教室被排课使用的次数
     *
     * @param classroomUuid 教室UUID
     * @return 排课记录数量
     */
    public long countByClassroomUuid(String classroomUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        return this.count(queryWrapper);
    }
}
