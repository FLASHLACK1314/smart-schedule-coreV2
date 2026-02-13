package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeachingClassClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeachingClassClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教学班-行政班级关联DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class TeachingClassClassDAO extends ServiceImpl<TeachingClassClassMapper, TeachingClassClassDO>
        implements IService<TeachingClassClassDO> {

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

    /**
     * 检查行政班级是否被关联表引用
     *
     * @param classUuid 行政班级UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByClassUuid(String classUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassClassDO::getClassUuid, classUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 检查教学班-行政班级关联是否已存在（防止重复添加）
     *
     * @param teachingClassUuid 教学班UUID
     * @param classUuid         行政班级UUID
     * @return 是否存在
     */
    public boolean existsByTeachingClassUuidAndClassUuid(String teachingClassUuid, String classUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        queryWrapper.eq(TeachingClassClassDO::getClassUuid, classUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询教学班-行政班关联
     *
     * @param page              页码
     * @param size              每页数量
     * @param teachingClassUuid 教学班UUID（可选过滤）
     * @param classUuid         行政班级UUID（可选过滤）
     * @return 分页结果
     */
    public IPage<TeachingClassClassDO> getTeachingClassClassPage(int page, int size,
            String teachingClassUuid, String classUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        if (teachingClassUuid != null && !teachingClassUuid.trim().isEmpty()) {
            queryWrapper.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        }
        if (classUuid != null && !classUuid.trim().isEmpty()) {
            queryWrapper.eq(TeachingClassClassDO::getClassUuid, classUuid);
        }
        return this.page(new Page<>(page, size), queryWrapper);
    }
}