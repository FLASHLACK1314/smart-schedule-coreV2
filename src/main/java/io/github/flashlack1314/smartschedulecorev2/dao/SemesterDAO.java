package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.SemesterMapper;
import io.github.flashlack1314.smartschedulecorev2.model.SemesterDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学期DAO
 * @author flash
 */
@Slf4j
@Repository
public class SemesterDAO extends ServiceImpl<SemesterMapper, SemesterDO>
        implements IService<SemesterDO> {
}
