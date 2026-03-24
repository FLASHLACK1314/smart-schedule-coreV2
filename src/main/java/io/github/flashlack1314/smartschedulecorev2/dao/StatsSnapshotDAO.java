package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.StatsSnapshotMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StatsSnapshotDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 统计快照DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class StatsSnapshotDAO extends ServiceImpl<StatsSnapshotMapper, StatsSnapshotDO>
        implements IService<StatsSnapshotDO> {

    /**
     * 根据周开始日期获取快照
     *
     * @param weekStartDate 周开始日期
     * @return 统计快照
     */
    public StatsSnapshotDO getByWeekStartDate(LocalDate weekStartDate) {
        LambdaQueryWrapper<StatsSnapshotDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatsSnapshotDO::getWeekStartDate, weekStartDate);
        return this.getOne(queryWrapper);
    }
}
