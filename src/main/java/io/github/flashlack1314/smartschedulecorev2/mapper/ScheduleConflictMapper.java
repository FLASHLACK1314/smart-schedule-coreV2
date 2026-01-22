package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ScheduleConflictDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排课冲突记录Mapper
 *
 * @author flash
 */
@Mapper
public interface ScheduleConflictMapper extends BaseMapper<ScheduleConflictDO> {
}
