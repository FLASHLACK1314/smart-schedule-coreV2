package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 行政班级DAO
 * @author flash
 */
@Slf4j
@Repository
public class ClassDAO extends ServiceImpl<ClassMapper, ClassDO>
        implements IService<ClassDO> {
}
