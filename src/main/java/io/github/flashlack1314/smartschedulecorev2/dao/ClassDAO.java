package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 行政班级DAO
 * @author flash
 */
@Slf4j
@Repository
public class ClassDAO extends ServiceImpl<ClassMapper, ClassDO>
        implements IService<ClassDO> {

    /**
     * 检查专业下是否存在班级
     *
     * @param majorUuid 专业UUID
     * @return 是否存在
     */
    public boolean existsByMajorUuid(String majorUuid) {
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassDO::getMajorUuid, majorUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计专业下的班级数量
     *
     * @param majorUuid 专业UUID
     * @return 班级数量
     */
    public long countByMajorUuid(String majorUuid) {
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassDO::getMajorUuid, majorUuid);
        return this.count(queryWrapper);
    }
}
