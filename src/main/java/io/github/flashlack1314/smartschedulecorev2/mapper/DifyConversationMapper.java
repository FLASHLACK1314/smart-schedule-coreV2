package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DifyConversationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dify会话关联表Mapper
 *
 * @author flash
 */
@Mapper
public interface DifyConversationMapper extends BaseMapper<DifyConversationDO> {
}
