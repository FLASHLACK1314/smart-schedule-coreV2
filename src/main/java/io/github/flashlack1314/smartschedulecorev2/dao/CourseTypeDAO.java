package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.CourseTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程类型DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseTypeDAO extends ServiceImpl<CourseTypeMapper, CourseTypeDO>
        implements IService<CourseTypeDO> {
}