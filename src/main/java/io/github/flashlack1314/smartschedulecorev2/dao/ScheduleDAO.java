package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ScheduleMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ScheduleDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 排课DAO
 * @author flash
 */
@Slf4j
@Repository
public class ScheduleDAO extends ServiceImpl<ScheduleMapper, ScheduleDO>
        implements IService<ScheduleDO> {

    /**
     * 检查教室是否被排课使用
     *
     * @param classroomUuid 教室UUID
     * @return 如果被使用返回true，否则返回false
     */
    public boolean existsByClassroomUuid(String classroomUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教室被排课使用的次数
     *
     * @param classroomUuid 教室UUID
     * @return 排课记录数量
     */
    public long countByClassroomUuid(String classroomUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        return this.count(queryWrapper);
    }

    /**
     * 检查教学班是否被排课使用
     *
     * @param teachingClassUuid 教学班UUID
     * @return 如果被使用返回true，否则返回false
     */
    public boolean existsByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getTeachingClassUuid, teachingClassUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计教学班被排课使用的次数
     *
     * @param teachingClassUuid 教学班UUID
     * @return 排课记录数量
     */
    public long countByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getTeachingClassUuid, teachingClassUuid);
        return this.count(queryWrapper);
    }

    /**
     * 分页查询排课记录
     *
     * @param page              页码
     * @param size              每页数量
     * @param semesterUuid      学期UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param classroomUuid     教室UUID（可选）
     * @param teacherUuid       教师UUID（可选）
     * @param dayOfWeek         星期几（可选）
     * @param status            状态（可选）
     * @return 分页结果
     */
    public IPage<ScheduleDO> getSchedulePage(int page, int size,
            String semesterUuid, String teachingClassUuid, String classroomUuid,
            String teacherUuid, Integer dayOfWeek, Integer status) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        }
        if (StringUtils.hasText(teachingClassUuid)) {
            queryWrapper.eq(ScheduleDO::getTeachingClassUuid, teachingClassUuid);
        }
        if (StringUtils.hasText(classroomUuid)) {
            queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        }
        if (StringUtils.hasText(teacherUuid)) {
            queryWrapper.eq(ScheduleDO::getTeacherUuid, teacherUuid);
        }
        if (dayOfWeek != null) {
            queryWrapper.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
        }
        if (status != null) {
            queryWrapper.eq(ScheduleDO::getStatus, status);
        }

        return this.page(new Page<>(page, size), queryWrapper);
    }

    /**
     * 按教师查询课表
     *
     * @param teacherUuid  教师UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    public List<ScheduleDO> getTeacherTimetable(String teacherUuid, String semesterUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getTeacherUuid, teacherUuid);
        queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        queryWrapper.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
        return this.list(queryWrapper);
    }

    /**
     * 按教室查询课表
     *
     * @param classroomUuid 教室UUID
     * @param semesterUuid  学期UUID
     * @return 课表列表
     */
    public List<ScheduleDO> getClassroomTimetable(String classroomUuid, String semesterUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        queryWrapper.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
        return this.list(queryWrapper);
    }

    /**
     * 按教学班查询课表
     *
     * @param teachingClassUuid 教学班UUID
     * @param semesterUuid      学期UUID
     * @return 课表列表
     */
    public List<ScheduleDO> getTeachingClassTimetable(String teachingClassUuid, String semesterUuid) {
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getTeachingClassUuid, teachingClassUuid);
        queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        queryWrapper.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
        return this.list(queryWrapper);
    }
}
