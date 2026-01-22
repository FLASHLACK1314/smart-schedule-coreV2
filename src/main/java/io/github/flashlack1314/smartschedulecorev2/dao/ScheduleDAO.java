package io.github.flashlack1314.smartschedulecorev2.dao;

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
}
