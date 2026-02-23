package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flashlack1314.smartschedulecorev2.algorithm.core.*;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleContext;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.Chromosome;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import io.github.flashlack1314.smartschedulecorev2.algorithm.util.TimeSlotGenerator;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动排课服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoScheduleServiceImpl implements AutoScheduleService {

    private final SemesterDAO semesterDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final ClassroomDAO classroomDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final StudentDAO studentDAO;
    private final ScheduleDAO scheduleDAO;
    private final ObjectMapper objectMapper;

    private final FitnessCalculator fitnessCalculator;
    private final ConflictDetector conflictDetector;
    private final TimeSlotGenerator timeSlotGenerator;
    private final HoursCalculator hoursCalculator;

    @Override
    public AutoScheduleResult autoSchedule(io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO request) {
        log.info("开始自动排课，学期UUID: {}, 教学班数量: {}",
                request.getSemesterUuid(),
                request.getTeachingClassUuids() != null ? request.getTeachingClassUuids().size() : 0);

        // 1. 构建排课上下文
        ScheduleContext context = buildScheduleContext(request);

        // 2. 初始化遗传算法
        GeneticAlgorithm ga = new GeneticAlgorithm(
                context,
                fitnessCalculator,
                conflictDetector,
                timeSlotGenerator,
                hoursCalculator
        );

        // 设置算法参数
        if (request.getPopulationSize() != null) {
            ga.setPopulationSize(request.getPopulationSize());
        }
        if (request.getMaxGenerations() != null) {
            ga.setMaxGenerations(request.getMaxGenerations());
        }
        if (request.getCrossoverRate() != null) {
            ga.setCrossoverRate(request.getCrossoverRate());
        }
        if (request.getMutationRate() != null) {
            ga.setMutationRate(request.getMutationRate());
        }
        if (request.getEliteSize() != null) {
            ga.setEliteSize(request.getEliteSize());
        }

        // 3. 执行算法
        Chromosome bestSolution = ga.schedule();

        // 4. 构建结果
        AutoScheduleResult result = buildResult(bestSolution, context, request.getSemesterUuid());

        log.info("自动排课完成，适应度: {}, 硬冲突数: {}, 未完成数: {}",
                result.getFitness(),
                result.getHardConflicts(),
                result.getUnscheduledTeachingClasses() != null ? result.getUnscheduledTeachingClasses().size() : 0);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScheduleResultAsPreview(AutoScheduleResult result, String semesterUuid) {
        log.info("保存排课方案为预览状态，学期UUID: {}", semesterUuid);

        // 先清除该学期的预览排课
        clearPreviewSchedules(semesterUuid);

        // 保存新的预览排课
        List<ScheduleDO> schedulesToSave = new ArrayList<>();

        for (Map.Entry<String, List<CourseAppointment>> entry : result.getScheduleMap().entrySet()) {
            String teachingClassUuid = entry.getKey();

            for (CourseAppointment appt : entry.getValue()) {
                ScheduleDO schedule = new ScheduleDO();
                schedule.setScheduleUuid(UUID.randomUUID().toString().replace("-", ""));
                schedule.setSemesterUuid(semesterUuid);
                schedule.setTeachingClassUuid(teachingClassUuid);
                schedule.setCourseUuid(appt.getCourseUuid());
                schedule.setTeacherUuid(appt.getTeacherUuid());
                schedule.setClassroomUuid(appt.getClassroomUuid());
                schedule.setDayOfWeek(appt.getTimeSlot().getDayOfWeek());
                schedule.setSectionStart(appt.getTimeSlot().getSectionStart());
                schedule.setSectionEnd(appt.getTimeSlot().getSectionEnd());

                // 转换周次列表为JSON字符串
                try {
                    String weeksJson = objectMapper.writeValueAsString(appt.getTimeSlot().getWeeks());
                    schedule.setWeeksJson(weeksJson);
                } catch (Exception e) {
                    log.error("转换周次JSON失败", e);
                    schedule.setWeeksJson("[]");
                }

                // 计算累计学时
                schedule.setCreditHours(appt.getTimeSlot().getTotalHours());

                // 设置为预览状态
                schedule.setStatus(0);
                schedule.setUpdatedAt(LocalDateTime.now());

                schedulesToSave.add(schedule);
            }
        }

        // 批量保存
        if (!schedulesToSave.isEmpty()) {
            scheduleDAO.saveBatch(schedulesToSave);
            log.info("保存了{}条预览排课记录", schedulesToSave.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSchedule(String semesterUuid) {
        log.info("确认排课方案，学期UUID: {}", semesterUuid);

        // 查询所有预览状态的排课记录
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        queryWrapper.eq(ScheduleDO::getStatus, 0);

        List<ScheduleDO> previewSchedules = scheduleDAO.list(queryWrapper);

        if (previewSchedules.isEmpty()) {
            log.warn("没有找到预览状态的排课记录");
            return;
        }

        // 将预览状态转为正式状态
        for (ScheduleDO schedule : previewSchedules) {
            schedule.setStatus(1);
            schedule.setUpdatedAt(LocalDateTime.now());
        }

        scheduleDAO.updateBatchById(previewSchedules);
        log.info("确认了{}条排课记录", previewSchedules.size());

        // 更新教学班的累计学时
        updateTeachingClassHours(semesterUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearPreviewSchedules(String semesterUuid) {
        log.info("清除预览排课方案，学期UUID: {}", semesterUuid);

        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        queryWrapper.eq(ScheduleDO::getStatus, 0);

        int count = (int) scheduleDAO.count(queryWrapper);
        scheduleDAO.remove(queryWrapper);

        log.info("清除了{}条预览排课记录", count);
    }

    @Override
    public AutoScheduleResult.ScheduleStatistics getScheduleStatistics(String semesterUuid) {
        AutoScheduleResult.ScheduleStatistics statistics = new AutoScheduleResult.ScheduleStatistics();

        // 统计该学期的所有教学班
        LambdaQueryWrapper<TeachingClassDO> tcQuery = new LambdaQueryWrapper<>();
        tcQuery.eq(TeachingClassDO::getSemesterUuid, semesterUuid);
        statistics.setTotalTeachingClasses((int) teachingClassDAO.count(tcQuery));

        // 统计已排课的教学班
        LambdaQueryWrapper<ScheduleDO> sQuery = new LambdaQueryWrapper<>();
        sQuery.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        sQuery.eq(ScheduleDO::getStatus, 1);
        List<ScheduleDO> schedules = scheduleDAO.list(sQuery);
        Set<String> scheduledClasses = schedules.stream()
                .map(ScheduleDO::getTeachingClassUuid)
                .collect(Collectors.toSet());
        statistics.setScheduledTeachingClasses(scheduledClasses.size());

        // 统计总课时数
        statistics.setTotalSessions(schedules.size());
        statistics.setTotalHours(schedules.stream()
                .mapToInt(ScheduleDO::getCreditHours)
                .sum());

        return statistics;
    }

    /**
     * 构建排课上下文
     */
    private ScheduleContext buildScheduleContext(io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO request) {
        ScheduleContext context = new ScheduleContext();

        // 查询学期信息
        SemesterDO semester = semesterDAO.getById(request.getSemesterUuid());
        if (semester == null) {
            throw new IllegalArgumentException("学期不存在: " + request.getSemesterUuid());
        }
        context.setSemesterUuid(semester.getSemesterUuid());
        context.setSemesterWeeks(semester.getSemesterWeeks());
        context.setStartDate(semester.getStartDate());
        context.setEndDate(semester.getEndDate());

        // 查询教学班信息
        List<TeachingClassDO> teachingClasses = teachingClassDAO.listByIds(request.getTeachingClassUuids());
        List<ScheduleContext.TeachingClassInfo> tcInfoList = new ArrayList<>();

        for (TeachingClassDO tc : teachingClasses) {
            ScheduleContext.TeachingClassInfo info = new ScheduleContext.TeachingClassInfo();

            info.setTeachingClassUuid(tc.getTeachingClassUuid());
            info.setTeachingClassName(tc.getTeachingClassName());
            info.setCourseUuid(tc.getCourseUuid());
            info.setTeacherUuid(tc.getTeacherUuid());

            // 查询课程信息
            CourseDO course = courseDAO.getById(tc.getCourseUuid());
            if (course == null) {
                log.warn("课程不存在: {}", tc.getCourseUuid());
                continue;
            }
            info.setCourseName(course.getCourseName());
            info.setCourseTypeUuid(course.getCourseTypeUuid());
            info.setCourseTotalHours(course.getCourseHours());

            // 查询教师信息
            TeacherDO teacher = teacherDAO.getById(tc.getTeacherUuid());
            if (teacher == null) {
                log.warn("教师不存在: {}", tc.getTeacherUuid());
                continue;
            }
            info.setTeacherName(teacher.getTeacherName());

            // 每周上课次数配置
            Integer weeklySessions = tc.getWeeklySessions();
            if (request.getWeeklySessionsConfig() != null &&
                    request.getWeeklySessionsConfig().containsKey(tc.getTeachingClassUuid())) {
                weeklySessions = request.getWeeklySessionsConfig().get(tc.getTeachingClassUuid());
            }
            info.setWeeklySessions(weeklySessions != null ? weeklySessions : 1);
            info.setSectionsPerSession(2);

            // 查询关联的行政班和学生总数
            LambdaQueryWrapper<TeachingClassClassDO> relationQuery = new LambdaQueryWrapper<>();
            relationQuery.eq(TeachingClassClassDO::getTeachingClassUuid, tc.getTeachingClassUuid());
            List<TeachingClassClassDO> relations = teachingClassClassDAO.list(relationQuery);

            List<String> classUuids = relations.stream()
                    .map(TeachingClassClassDO::getClassUuid)
                    .collect(Collectors.toList());
            info.setClassUuids(classUuids);

            int totalStudents = calculateTotalStudents(classUuids);
            info.setTotalStudents(totalStudents);

            tcInfoList.add(info);
        }
        context.setTeachingClassList(tcInfoList);

        // 查询可用教室
        if (request.getClassroomUuids() != null && !request.getClassroomUuids().isEmpty()) {
            // 使用指定的教室列表
            Map<String, List<ScheduleContext.ClassroomInfo>> classroomsMap = new HashMap<>();
            List<ClassroomDO> classrooms = classroomDAO.listByIds(request.getClassroomUuids());

            for (ClassroomDO classroom : classrooms) {
                ScheduleContext.ClassroomInfo info = new ScheduleContext.ClassroomInfo();
                info.setClassroomUuid(classroom.getClassroomUuid());
                info.setClassroomName(classroom.getClassroomName());
                info.setCapacity(classroom.getClassroomCapacity());
                info.setClassroomTypeUuid(classroom.getClassroomTypeUuid());

                classroomsMap.computeIfAbsent(classroom.getClassroomTypeUuid(), k -> new ArrayList<>())
                        .add(info);
            }
            context.setAvailableClassrooms(classroomsMap);
        } else {
            // 使用所有可用教室
            List<ClassroomDO> allClassrooms = classroomDAO.list();
            Map<String, List<ScheduleContext.ClassroomInfo>> classroomsMap = new HashMap<>();

            for (ClassroomDO classroom : allClassrooms) {
                ScheduleContext.ClassroomInfo info = new ScheduleContext.ClassroomInfo();
                info.setClassroomUuid(classroom.getClassroomUuid());
                info.setClassroomName(classroom.getClassroomName());
                info.setCapacity(classroom.getClassroomCapacity());
                info.setClassroomTypeUuid(classroom.getClassroomTypeUuid());

                classroomsMap.computeIfAbsent(classroom.getClassroomTypeUuid(), k -> new ArrayList<>())
                        .add(info);
            }
            context.setAvailableClassrooms(classroomsMap);
        }

        // 查询已有的正式排课记录（status=1）
        LambdaQueryWrapper<ScheduleDO> existingQuery = new LambdaQueryWrapper<>();
        existingQuery.eq(ScheduleDO::getSemesterUuid, request.getSemesterUuid());
        existingQuery.eq(ScheduleDO::getStatus, 1);
        List<ScheduleDO> existingSchedules = scheduleDAO.list(existingQuery);

        List<ScheduleContext.ExistingSchedule> existingList = existingSchedules.stream()
                .map(this::convertToExistingSchedule)
                .collect(Collectors.toList());
        context.setExistingSchedules(existingList);

        return context;
    }

    /**
     * 构建排课结果
     */
    private AutoScheduleResult buildResult(Chromosome solution, ScheduleContext context, String semesterUuid) {
        AutoScheduleResult result = new AutoScheduleResult();
        result.setSemesterUuid(semesterUuid);
        result.setFitness(solution.getFitness());
        result.setHardConflicts(solution.getHardConstraintViolations());
        result.setSoftConflicts(solution.getSoftConstraintViolations());
        result.setUnscheduledTeachingClasses(solution.getUnscheduledTeachingClasses());

        // 转换为按教学班分组的结果
        Map<String, List<CourseAppointment>> scheduleMap = new HashMap<>();

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : solution.getGenes().entrySet()) {
            for (CourseAppointment appt : entry.getValue()) {
                scheduleMap.computeIfAbsent(appt.getTeachingClassUuid(), k -> new ArrayList<>())
                        .add(appt);
            }
        }

        result.setScheduleMap(scheduleMap);

        // 构建统计信息
        AutoScheduleResult.ScheduleStatistics statistics = new AutoScheduleResult.ScheduleStatistics();
        statistics.setTotalTeachingClasses(context.getTeachingClassList().size());
        statistics.setScheduledTeachingClasses(
                statistics.getTotalTeachingClasses() - result.getUnscheduledTeachingClasses().size()
        );

        int totalSessions = scheduleMap.values().stream()
                .mapToInt(List::size)
                .sum();
        statistics.setTotalSessions(totalSessions);

        int totalHours = scheduleMap.values().stream()
                .flatMap(List::stream)
                .mapToInt(appt -> appt.getTimeSlot().getTotalHours())
                .sum();
        statistics.setTotalHours(totalHours);

        statistics.setAverageFitness(result.getFitness());

        result.setStatistics(statistics);

        return result;
    }

    /**
     * 转换排课记录为已有排课信息
     */
    private ScheduleContext.ExistingSchedule convertToExistingSchedule(ScheduleDO schedule) {
        ScheduleContext.ExistingSchedule existing = new ScheduleContext.ExistingSchedule();
        existing.setScheduleUuid(schedule.getScheduleUuid());
        existing.setTeachingClassUuid(schedule.getTeachingClassUuid());
        existing.setTeacherUuid(schedule.getTeacherUuid());
        existing.setClassroomUuid(schedule.getClassroomUuid());

        // 解析周次JSON
        List<Integer> weeks = new ArrayList<>();
        try {
            weeks = objectMapper.readValue(schedule.getWeeksJson(), new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            log.error("解析周次JSON失败: {}", schedule.getWeeksJson(), e);
        }

        // 构建时间槽
        TimeSlot slot = new TimeSlot();
        slot.setDayOfWeek(schedule.getDayOfWeek());
        slot.setSectionStart(schedule.getSectionStart());
        slot.setSectionEnd(schedule.getSectionEnd());
        slot.setWeeks(weeks);
        existing.setTimeSlot(slot);

        // 查询关联的行政班
        LambdaQueryWrapper<TeachingClassClassDO> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(TeachingClassClassDO::getTeachingClassUuid, schedule.getTeachingClassUuid());
        List<TeachingClassClassDO> relations = teachingClassClassDAO.list(relationQuery);
        List<String> classUuids = relations.stream()
                .map(TeachingClassClassDO::getClassUuid)
                .collect(Collectors.toList());
        existing.setClassUuids(classUuids);

        return existing;
    }

    /**
     * 计算学生总数
     */
    private int calculateTotalStudents(List<String> classUuids) {
        if (classUuids == null || classUuids.isEmpty()) {
            return 0;
        }

        return classUuids.stream()
                .mapToInt(classUuid -> {
                    LambdaQueryWrapper<io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO> query =
                            new LambdaQueryWrapper<>();
                    query.eq(io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO::getClassUuid, classUuid);
                    return (int) studentDAO.count(query);
                })
                .sum();
    }

    /**
     * 更新教学班的累计学时
     */
    private void updateTeachingClassHours(String semesterUuid) {
        // 查询该学期的所有正式排课记录
        LambdaQueryWrapper<ScheduleDO> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        query.eq(ScheduleDO::getStatus, 1);
        List<ScheduleDO> schedules = scheduleDAO.list(query);

        // 按教学班分组统计学时
        Map<String, Integer> hoursMap = schedules.stream()
                .collect(Collectors.groupingBy(
                        ScheduleDO::getTeachingClassUuid,
                        Collectors.summingInt(ScheduleDO::getCreditHours)
                ));

        // 更新教学班表
        for (Map.Entry<String, Integer> entry : hoursMap.entrySet()) {
            TeachingClassDO tc = teachingClassDAO.getById(entry.getKey());
            if (tc != null) {
                tc.setTeachingClassHours(entry.getValue());
                teachingClassDAO.updateById(tc);
            }
        }
    }
}
