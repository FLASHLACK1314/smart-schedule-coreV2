package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ScheduleConflictMapper;
import io.github.flashlack1314.smartschedulecorev2.model.ScheduleConflictDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 排课冲突记录DAO
 * @author flash
 */
@Slf4j
@Repository
public class ScheduleConflictDAO extends ServiceImpl<ScheduleConflictMapper, ScheduleConflictDO>
        implements IService<ScheduleConflictDO> {
}
