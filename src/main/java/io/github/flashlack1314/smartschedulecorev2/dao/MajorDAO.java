package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.MajorMapper;
import io.github.flashlack1314.smartschedulecorev2.model.MajorDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 专业DAO
 * @author flash
 */
@Slf4j
@Repository
public class MajorDAO extends ServiceImpl<MajorMapper, MajorDO>
        implements IService<MajorDO> {
}
