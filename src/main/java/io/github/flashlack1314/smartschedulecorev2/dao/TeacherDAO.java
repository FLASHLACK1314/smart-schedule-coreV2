package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeacherMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教师DAO
 * @author flash
 */
@Slf4j
@Repository
public class TeacherDAO extends ServiceImpl<TeacherMapper, TeacherDO>
        implements IService<TeacherDO> {
}
