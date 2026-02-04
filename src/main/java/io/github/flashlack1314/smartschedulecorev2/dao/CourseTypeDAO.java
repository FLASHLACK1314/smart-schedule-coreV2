package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程类型DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseTypeDAO extends ServiceImpl<CourseTypeMapper, CourseTypeDO>
        implements IService<CourseTypeDO> {

    /**
     * 根据课程类型名称获取课程类型
     *
     * @param courseTypeName 课程类型名称
     * @return 课程类型实体
     */
    public CourseTypeDO getByCourseTypeName(String courseTypeName) {
        LambdaQueryWrapper<CourseTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTypeDO::getTypeName, courseTypeName);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查课程类型名称是否存在
     *
     * @param courseTypeName 课程类型名称
     * @return 是否存在
     */
    public boolean existsByCourseTypeName(String courseTypeName) {
        LambdaQueryWrapper<CourseTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTypeDO::getTypeName, courseTypeName);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 检查课程类型名称是否被其他课程类型使用
     *
     * @param courseTypeName 课程类型名称
     * @param excludeUuid    排除的UUID
     * @return 是否存在
     */
    public boolean existsByCourseTypeNameExcludeUuid(String courseTypeName, String excludeUuid) {
        LambdaQueryWrapper<CourseTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTypeDO::getTypeName, courseTypeName);
        queryWrapper.ne(CourseTypeDO::getCourseTypeUuid, excludeUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询课程类型
     *
     * @param page           页码
     * @param size           每页数量
     * @param courseTypeName 课程类型名称（可选，模糊查询）
     * @return 分页结果
     */
    public IPage<CourseTypeDO> getCourseTypePage(int page, int size, String courseTypeName) {
        LambdaQueryWrapper<CourseTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        if (courseTypeName != null && !courseTypeName.trim().isEmpty()) {
            queryWrapper.like(CourseTypeDO::getTypeName, courseTypeName);
        }
        return this.page(new Page<>(page, size), queryWrapper);
    }
}