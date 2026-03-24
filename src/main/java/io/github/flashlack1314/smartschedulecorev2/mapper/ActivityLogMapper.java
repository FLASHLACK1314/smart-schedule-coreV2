package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ActivityLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动记录Mapper
 *
 * @author flash
 */
@Mapper
public interface ActivityLogMapper extends BaseMapper<ActivityLogDO> {
}
