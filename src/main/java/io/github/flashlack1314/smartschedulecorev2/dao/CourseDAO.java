package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 课程DAO
 * @author flash
 */
@Slf4j
@Repository
public class CourseDAO extends ServiceImpl<CourseMapper, CourseDO>
        implements IService<CourseDO> {

    /**
     * 根据课程编号查询课程
     *
     * @param courseNum 课程编号
     * @return 课程实体，如果不存在则返回null
     */
    public CourseDO getByCourseNum(String courseNum) {
        LambdaQueryWrapper<CourseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseDO::getCourseNum, courseNum);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查课程编号是否已存在
     *
     * @param courseNum 课程编号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByCourseNum(String courseNum) {
        return getByCourseNum(courseNum) != null;
    }

    /**
     * 检查课程编号是否被其他课程使用（排除指定UUID）
     *
     * @param courseNum  课程编号
     * @param courseUuid 要排除的课程UUID
     * @return 是否存在
     */
    public boolean existsByCourseNumExcludeUuid(String courseNum, String courseUuid) {
        LambdaQueryWrapper<CourseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseDO::getCourseNum, courseNum)
                .ne(CourseDO::getCourseUuid, courseUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询课程
     *
     * @param page           页码
     * @param size           每页数量
     * @param courseName     课程名称（可选，模糊查询）
     * @param courseNum      课程编号（可选，模糊查询）
     * @param courseTypeUuid 课程类型UUID（可选）
     * @return 分页结果
     */
    public IPage<CourseDO> getCoursePage(int page, int size, String courseName, String courseNum, String courseTypeUuid) {
        Page<CourseDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseDO> queryWrapper = new LambdaQueryWrapper<>();

        // 课程名称模糊查询
        if (StringUtils.hasText(courseName)) {
            queryWrapper.like(CourseDO::getCourseName, courseName);
        }

        // 课程编号模糊查询
        if (StringUtils.hasText(courseNum)) {
            queryWrapper.like(CourseDO::getCourseNum, courseNum);
        }

        // 课程类型UUID精确查询
        if (StringUtils.hasText(courseTypeUuid)) {
            queryWrapper.eq(CourseDO::getCourseTypeUuid, courseTypeUuid);
        }

        return this.page(pageParam, queryWrapper);
    }
}
