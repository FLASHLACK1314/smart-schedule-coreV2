package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseClassroomTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseClassroomTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程类型-教室类型关联DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseClassroomTypeDAO extends ServiceImpl<CourseClassroomTypeMapper, CourseClassroomTypeDO>
        implements IService<CourseClassroomTypeDO> {
}