package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeachingClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeachingClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教学班DAO
 * @author flash
 */
@Slf4j
@Repository
public class TeachingClassDAO extends ServiceImpl<TeachingClassMapper, TeachingClassDO>
        implements IService<TeachingClassDO> {

    /**
     * 检查教师是否被教学班引用
     *
     * @param teacherUuid 教师UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByTeacherUuid(String teacherUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教师被教学班引用的数量
     *
     * @param teacherUuid 教师UUID
     * @return 引用数量
     */
    public long countByTeacherUuid(String teacherUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper);
    }
}
