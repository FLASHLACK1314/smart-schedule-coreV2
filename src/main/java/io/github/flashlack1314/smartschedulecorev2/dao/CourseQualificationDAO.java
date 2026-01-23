package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseQualificationMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseQualificationDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程教师资格关联DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseQualificationDAO extends ServiceImpl<CourseQualificationMapper, CourseQualificationDO>
        implements IService<CourseQualificationDO> {
}
