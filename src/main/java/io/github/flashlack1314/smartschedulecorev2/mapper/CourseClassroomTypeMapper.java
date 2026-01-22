package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.CourseClassroomTypeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程类型-教室类型关联Mapper
 *
 * @author flash
 */
@Mapper
public interface CourseClassroomTypeMapper extends BaseMapper<CourseClassroomTypeDO> {
}