package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassroomMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教室DAO
 * @author flash
 */
@Slf4j
@Repository
public class ClassroomDAO extends ServiceImpl<ClassroomMapper, ClassroomDO>
        implements IService<ClassroomDO> {
}
