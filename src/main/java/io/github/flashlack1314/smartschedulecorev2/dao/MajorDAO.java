package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.MajorMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.MajorDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 专业DAO
 * @author flash
 */
@Slf4j
@Repository
public class MajorDAO extends ServiceImpl<MajorMapper, MajorDO>
        implements IService<MajorDO> {

    /**
     * 检查学院下是否存在专业
     *
     * @param departmentUuid 学院UUID
     * @return 是否存在
     */
    public boolean existsByDepartmentUuid(String departmentUuid) {
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MajorDO::getDepartmentUuid, departmentUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计学院下的专业数量
     *
     * @param departmentUuid 学院UUID
     * @return 专业数量
     */
    public long countByDepartmentUuid(String departmentUuid) {
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MajorDO::getDepartmentUuid, departmentUuid);
        return this.count(queryWrapper);
    }
}
