package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    /**
     * 检查班级名称在同一专业下是否已存在
     *
     * @param className 班级名称
     * @param majorUuid 专业UUID
     * @return 是否存在
     */
    public boolean existsByClassNameAndMajorUuid(String className, String majorUuid) {
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassDO::getClassName, className)
                .eq(ClassDO::getMajorUuid, majorUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 检查班级名称在同一专业下是否被其他班级使用（排除指定UUID）
     *
     * @param className 班级名称
     * @param majorUuid 专业UUID
     * @param classUuid 要排除的班级UUID
     * @return 是否存在
     */
    public boolean existsByClassNameAndMajorUuidExcludeUuid(String className, String majorUuid, String classUuid) {
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassDO::getClassName, className)
                .eq(ClassDO::getMajorUuid, majorUuid)
                .ne(ClassDO::getClassUuid, classUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询行政班级
     *
     * @param page           页码
     * @param size           每页数量
     * @param className      班级名称（可选，模糊查询）
     * @param majorUuid      专业UUID（可选）
     * @param departmentUuid 学院UUID（可选，需要关联查询）
     * @return 分页结果
     */
    public IPage<ClassDO> getClassPage(int page, int size, String className, String majorUuid, String departmentUuid) {
        Page<ClassDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();

        // 班级名称模糊查询
        if (StringUtils.hasText(className)) {
            queryWrapper.like(ClassDO::getClassName, className);
        }

        // 专业UUID精确查询
        if (StringUtils.hasText(majorUuid)) {
            queryWrapper.eq(ClassDO::getMajorUuid, majorUuid);
        }

        // 如果需要按学院查询，需要先查询该学院下的所有专业UUID
        // 这部分逻辑在Service层处理

        return this.page(pageParam, queryWrapper);
    }
}
