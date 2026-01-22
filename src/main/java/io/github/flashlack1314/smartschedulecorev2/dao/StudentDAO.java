package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.StudentMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学生DAO
 * @author flash
 */
@Slf4j
@Repository
public class StudentDAO extends ServiceImpl<StudentMapper, StudentDO>
        implements IService<StudentDO> {
}
