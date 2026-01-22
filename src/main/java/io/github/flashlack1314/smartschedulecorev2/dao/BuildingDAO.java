package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.BuildingMapper;
import io.github.flashlack1314.smartschedulecorev2.model.BuildingDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教学楼DAO
 * @author flash
 */
@Slf4j
@Repository
public class BuildingDAO extends ServiceImpl<BuildingMapper, BuildingDO>
        implements IService<BuildingDO> {
}
