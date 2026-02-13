package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeachingClassClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeachingClassClassDO;
import org.springframework.stereotype.Repository;

/**
 * 教学班-行政班级关联DAO
 *
 * @author flash
 */
@Repository
public class TeachingClassClassDAO extends ServiceImpl<TeachingClassClassMapper, TeachingClassClassDO> {

    /**
     * 检查教学班是否被关联表引用
     *
     * @param teachingClassUuid 教学班UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教学班被关联表引用的数量
     *
     * @param teachingClassUuid 教学班UUID
     * @return 引用数量
     */
    public long countByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        return this.count(queryWrapper);
    }
}