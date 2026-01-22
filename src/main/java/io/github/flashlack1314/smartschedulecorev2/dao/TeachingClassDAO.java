package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeachingClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.TeachingClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教学班DAO
 * @author flash
 */
@Slf4j
@Repository
public class TeachingClassDAO extends ServiceImpl<TeachingClassMapper, TeachingClassDO>
        implements IService<TeachingClassDO> {
}
