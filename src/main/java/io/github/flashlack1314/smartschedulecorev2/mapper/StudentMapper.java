package io.github.flashlack1314.smartschedulecorev2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生Mapper
 *
 * @author flash
 */
@Mapper
public interface StudentMapper extends BaseMapper<StudentDO> {
}
