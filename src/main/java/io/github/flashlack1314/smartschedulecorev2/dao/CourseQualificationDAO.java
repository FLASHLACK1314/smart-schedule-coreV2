package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
