package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseQualificationMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseQualificationDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程教师资格关联DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseQualificationDAO extends ServiceImpl<CourseQualificationMapper, CourseQualificationDO>
        implements IService<CourseQualificationDO> {

    /**
     * 检查教师是否被课程资格关联表引用
     *
     * @param teacherUuid 教师UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByTeacherUuid(String teacherUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseQualificationDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教师被课程资格关联表引用的数量
     *
     * @param teacherUuid 教师UUID
     * @return 引用数量
     */
    public long countByTeacherUuid(String teacherUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseQualificationDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper);
    }

    /**
     * 检查课程是否被课程资格关联表引用
     *
     * @param courseUuid 课程UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByCourseUuid(String courseUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseQualificationDO::getCourseUuid, courseUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计课程被课程资格关联表引用的数量
     *
     * @param courseUuid 课程UUID
     * @return 引用数量
     */
    public long countByCourseUuid(String courseUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseQualificationDO::getCourseUuid, courseUuid);
        return this.count(queryWrapper);
    }

    /**
     * 检查课程-教师资格关联是否存在
     *
     * @param courseUuid  课程UUID
     * @param teacherUuid 教师UUID
     * @return 是否存在
     */
    public boolean existsByCourseUuidAndTeacherUuid(String courseUuid, String teacherUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseQualificationDO::getCourseUuid, courseUuid);
        queryWrapper.eq(CourseQualificationDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询资格关联信息
     *
     * @param page        页码
     * @param size        每页数量
     * @param courseUuid  课程UUID（可选过滤）
     * @param teacherUuid 教师UUID（可选过滤）
     * @return 分页结果
     */
    public IPage<CourseQualificationDO> getQualificationPage(int page, int size,
                                                             String courseUuid, String teacherUuid) {
        LambdaQueryWrapper<CourseQualificationDO> queryWrapper = new LambdaQueryWrapper<>();

        if (courseUuid != null && !courseUuid.trim().isEmpty()) {
            queryWrapper.eq(CourseQualificationDO::getCourseUuid, courseUuid);
        }

        if (teacherUuid != null && !teacherUuid.trim().isEmpty()) {
            queryWrapper.eq(CourseQualificationDO::getTeacherUuid, teacherUuid);
        }

        return this.page(new Page<>(page, size), queryWrapper);
    }
}
