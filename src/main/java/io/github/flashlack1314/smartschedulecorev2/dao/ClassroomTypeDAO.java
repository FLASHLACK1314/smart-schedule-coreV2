package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassroomTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教室类型DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class ClassroomTypeDAO extends ServiceImpl<ClassroomTypeMapper, ClassroomTypeDO>
        implements IService<ClassroomTypeDO> {

    /**
     * 检查教室类型UUID是否存在
     *
     * @param classroomTypeUuid 教室类型UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByUuid(String classroomTypeUuid) {
        return this.getById(classroomTypeUuid) != null;
    }
}