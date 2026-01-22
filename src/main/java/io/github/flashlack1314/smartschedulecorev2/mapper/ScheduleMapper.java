package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.ScheduleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排课Mapper
 *
 * @author flash
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<ScheduleDO> {
}
