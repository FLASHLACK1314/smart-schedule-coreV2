package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeachingClassMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeachingClassDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    /**
     * 检查课程是否被教学班引用
     *
     * @param courseUuid 课程UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsByCourseUuid(String courseUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getCourseUuid, courseUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计课程被教学班引用的数量
     *
     * @param courseUuid 课程UUID
     * @return 引用数量
     */
    public long countByCourseUuid(String courseUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getCourseUuid, courseUuid);
        return this.count(queryWrapper);
    }

    /**
     * 检查学期是否被教学班引用
     *
     * @param semesterUuid 学期UUID
     * @return 如果被引用返回true，否则返回false
     */
    public boolean existsBySemesterUuid(String semesterUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getSemesterUuid, semesterUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计学期被教学班引用的数量
     *
     * @param semesterUuid 学期UUID
     * @return 引用数量
     */
    public long countBySemesterUuid(String semesterUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingClassDO::getSemesterUuid, semesterUuid);
        return this.count(queryWrapper);
    }

    /**
     * 分页查询教学班
     *
     * @param page         页码
     * @param size         每页数量
     * @param courseUuid   课程UUID（可选过滤）
     * @param teacherUuid  教师UUID（可选过滤）
     * @param semesterUuid 学期UUID（可选过滤）
     * @return 分页结果
     */
    public IPage<TeachingClassDO> getTeachingClassPage(int page, int size,
                                                         String courseUuid, String teacherUuid, String semesterUuid) {
        LambdaQueryWrapper<TeachingClassDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(courseUuid)) {
            queryWrapper.eq(TeachingClassDO::getCourseUuid, courseUuid);
        }

        if (StringUtils.hasText(teacherUuid)) {
            queryWrapper.eq(TeachingClassDO::getTeacherUuid, teacherUuid);
        }

        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(TeachingClassDO::getSemesterUuid, semesterUuid);
        }

        return this.page(new Page<>(page, size), queryWrapper);
    }
}
