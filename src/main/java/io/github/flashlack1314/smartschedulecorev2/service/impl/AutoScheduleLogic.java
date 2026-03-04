package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;
import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动排课逻辑类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoScheduleLogic implements AutoScheduleService {

    private final SemesterDAO semesterDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final TeacherDAO teacherDAO;
    private final ClassroomDAO classroomDAO;
    private final CourseDAO courseDAO;
    private final ScheduleDAO scheduleDAO;
    private final CourseQualificationDAO courseQualificationDAO;
    private final CourseClassroomTypeDAO courseClassroomTypeDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final StudentDAO studentDAO;
    private final ClassDAO classDAO;
    private final ConflictDetector conflictDetector;
    private final FitnessCalculator fitnessCalculator;
    private final HoursCalculator hoursCalculator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行自动排课
     *
     * @param params 排课参数
     * @return 排课结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoScheduleResult execute(AutoScheduleVO params) {
        // 0. 参数验证
        params.validate();

        // 解析并创建教学班
        List<String> teachingClassUuids = parseAndCreateTeachingClasses(params);
        log.info("开始执行自动排课，学期UUID: {}, 教学班数量: {}",
                params.getSemesterUuid(), teachingClassUuids.size());

        // 1. 检查是否覆盖已有排课
        if (Boolean.TRUE.equals(params.getOverwrite())) {
            log.info("检测到 overwrite=true，删除已有排课记录");
            scheduleDAO.remove(
                    new QueryWrapper<ScheduleDO>()
                            .eq("semester_uuid", params.getSemesterUuid())
                            .in("teaching_class_uuid", teachingClassUuids)
            );
        }

        // 2. 构建排课上下文
        ScheduleContext context = buildScheduleContext(params, teachingClassUuids);

        // 3. 创建遗传算法实例（手动注入依赖）
        TimeSlotGenerator timeSlotGenerator = new TimeSlotGenerator();
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(
                context,
                fitnessCalculator,
                conflictDetector,
                timeSlotGenerator,
                hoursCalculator
        );

        // 4. 设置算法参数
        if (params.getPopulationSize() != null) {
            geneticAlgorithm.setPopulationSize(params.getPopulationSize());
        }
        if (params.getMaxGenerations() != null) {
            geneticAlgorithm.setMaxGenerations(params.getMaxGenerations());
        }
        if (params.getCrossoverRate() != null) {
            geneticAlgorithm.setCrossoverRate(params.getCrossoverRate());
        }
        if (params.getMutationRate() != null) {
            geneticAlgorithm.setMutationRate(params.getMutationRate());
        }
        if (params.getEliteSize() != null) {
            geneticAlgorithm.setEliteSize(params.getEliteSize());
        }

        // 5. 执行遗传算法
        Chromosome bestChromosome = geneticAlgorithm.schedule();

        // 6. 转换结果为 AutoScheduleResult
        AutoScheduleResult result = new AutoScheduleResult();
        result.setSemesterUuid(params.getSemesterUuid());
        result.setScheduleMap(bestChromosome.getGenes());
        result.setFitness(bestChromosome.getFitness());
        result.setHardConflicts(bestChromosome.getHardConstraintViolations());
        result.setSoftConflicts(bestChromosome.getSoftConstraintViolations());
        result.setUnscheduledTeachingClasses(bestChromosome.getUnscheduledTeachingClasses());

        // 7. 构建冲突报告
        result.setConflictReport(conflictDetector.detectConflicts(bestChromosome, context));

        // 8. 保存排课记录到数据库
        saveScheduleRecords(result, params.getScheduleMode(), context);

        // 9. 构建统计信息
        AutoScheduleResult.ScheduleStatistics statistics = new AutoScheduleResult.ScheduleStatistics();
        statistics.setTotalTeachingClasses(teachingClassUuids.size());
        statistics.setScheduledTeachingClasses(
                teachingClassUuids.size() - bestChromosome.getUnscheduledTeachingClasses().size()
        );

        int totalSessions = 0;
        int totalHours = 0;
        for (List<CourseAppointment> appointments : bestChromosome.getGenes().values()) {
            for (CourseAppointment appt : appointments) {
                totalSessions++;
                totalHours += appt.getTimeSlot().getTotalHours();
            }
        }
        statistics.setTotalSessions(totalSessions);
        statistics.setTotalHours(totalHours);
        statistics.setAverageFitness(bestChromosome.getFitness());
        result.setStatistics(statistics);

        log.info("自动排课完成，适应度: {}, 硬约束冲突: {}, 未排课教学班: {}",
                result.getFitness(), result.getHardConflicts(), result.getUnscheduledTeachingClasses().size());

        return result;
    }

    /**
     * 构建排课上下文
     */
    private ScheduleContext buildScheduleContext(AutoScheduleVO params, List<String> teachingClassUuids) {
        ScheduleContext context = new ScheduleContext();

        // 1. 查询学期信息
        SemesterDO semester = semesterDAO.getById(params.getSemesterUuid());
        if (semester == null) {
            throw new IllegalArgumentException("学期不存在: " + params.getSemesterUuid());
        }
        context.setSemesterUuid(semester.getSemesterUuid());
        context.setSemesterWeeks(semester.getSemesterWeeks());
        context.setStartDate(semester.getStartDate());
        context.setEndDate(semester.getEndDate());

        // 2. 查询教学班信息
        List<TeachingClassDO> teachingClasses = teachingClassDAO.listByIds(teachingClassUuids);
        List<ScheduleContext.TeachingClassInfo> teachingClassList = new ArrayList<>();

        for (TeachingClassDO tc : teachingClasses) {
            // 查询课程信息
            CourseDO course = courseDAO.getById(tc.getCourseUuid());
            if (course == null) {
                log.warn("课程不存在: {}, 跳过教学班: {}", tc.getCourseUuid(), tc.getTeachingClassUuid());
                continue;
            }

            // 查询教师信息
            TeacherDO teacher = teacherDAO.getById(tc.getTeacherUuid());
            if (teacher == null) {
                log.warn("教师不存在: {}, 跳过教学班: {}", tc.getTeacherUuid(), tc.getTeachingClassUuid());
                continue;
            }

            // 查询关联的行政班
            List<TeachingClassClassDO> teachingClassClasses = teachingClassClassDAO.list(
                    new QueryWrapper<TeachingClassClassDO>()
                            .eq("teaching_class_uuid", tc.getTeachingClassUuid())
            );

            // 计算学生总人数
            int totalStudents = 0;
            List<String> classUuids = teachingClassClasses.stream()
                    .map(TeachingClassClassDO::getClassUuid)
                    .collect(Collectors.toList());

            for (String classUuid : classUuids) {
                List<StudentDO> students = studentDAO.list(
                        new QueryWrapper<StudentDO>().eq("class_uuid", classUuid)
                );
                totalStudents += students.size();
            }

            // 计算需要的上课次数
            int hoursPerSession = 2; // 固定2节
            int requiredSessions = hoursCalculator.calculateRequiredSessions(course.getCourseHours(), hoursPerSession);

            // 使用教学班默认值
            int weeklySessions = (tc.getWeeklySessions() != null ? tc.getWeeklySessions() : 1);
            int sectionsPerSession = (tc.getSectionsPerSession() != null ? tc.getSectionsPerSession() : 2);

            ScheduleContext.TeachingClassInfo info = new ScheduleContext.TeachingClassInfo();
            info.setTeachingClassUuid(tc.getTeachingClassUuid());
            info.setTeachingClassName(tc.getTeachingClassName());
            info.setCourseUuid(tc.getCourseUuid());
            info.setCourseName(course.getCourseName());
            info.setCourseTypeUuid(course.getCourseTypeUuid());
            info.setCourseTotalHours(course.getCourseHours());
            info.setTeacherUuid(tc.getTeacherUuid());
            info.setTeacherName(teacher.getTeacherName());
            info.setClassUuids(classUuids);
            info.setTotalStudents(totalStudents);
            info.setWeeklySessions(weeklySessions);
            info.setSectionsPerSession(sectionsPerSession);
            info.setRequiredSessions(requiredSessions);

            teachingClassList.add(info);
        }
        context.setTeachingClassList(teachingClassList);

        // 3. 查询教室信息（按教学楼筛选）
        QueryWrapper<ClassroomDO> classroomQuery = new QueryWrapper<>();

        if (params.getBuildingUuids() != null && !params.getBuildingUuids().isEmpty()) {
            classroomQuery.in("building_uuid", params.getBuildingUuids());
        }

        List<ClassroomDO> classrooms = classroomDAO.list(classroomQuery);

        // 按教室类型分组
        Map<String, List<ScheduleContext.ClassroomInfo>> availableClassrooms = classrooms.stream()
                .collect(Collectors.groupingBy(
                        ClassroomDO::getClassroomTypeUuid,
                        Collectors.mapping(
                                cr -> {
                                    ScheduleContext.ClassroomInfo info = new ScheduleContext.ClassroomInfo();
                                    info.setClassroomUuid(cr.getClassroomUuid());
                                    info.setClassroomName(cr.getClassroomName());
                                    info.setCapacity(cr.getClassroomCapacity());
                                    info.setClassroomTypeUuid(cr.getClassroomTypeUuid());
                                    return info;
                                },
                                Collectors.toList()
                        )
                ));
        context.setAvailableClassrooms(availableClassrooms);

        // 3.1 查询课程类型-教室类型映射关系
        List<CourseClassroomTypeDO> courseClassroomTypes = courseClassroomTypeDAO.list();
        Map<String, List<String>> courseTypeToClassroomTypes = courseClassroomTypes.stream()
                .collect(Collectors.groupingBy(
                        CourseClassroomTypeDO::getCourseTypeUuid,
                        Collectors.mapping(
                                CourseClassroomTypeDO::getClassroomTypeUuid,
                                Collectors.toList()
                        )
                ));
        context.setCourseTypeToClassroomTypes(courseTypeToClassroomTypes);
        log.info("加载课程类型-教室类型映射: {} 条", courseClassroomTypes.size());

        // 4. 查询教师时间偏好和工作量限制
        Map<String, List<TimeSlot>> teacherTimePreferences = new HashMap<>();
        Map<String, Integer> teacherMaxHours = new HashMap<>();

        Set<String> teacherUuids = teachingClassList.stream()
                .map(ScheduleContext.TeachingClassInfo::getTeacherUuid)
                .collect(Collectors.toSet());

        for (String teacherUuid : teacherUuids) {
            TeacherDO teacher = teacherDAO.getById(teacherUuid);
            if (teacher != null) {
                teacherMaxHours.put(teacherUuid, teacher.getMaxHoursPerWeek());

                // 解析教师时间偏好
                if (teacher.getLikeTime() != null && !teacher.getLikeTime().isEmpty()) {
                    List<TimeSlot> preferences = parseTeacherPreferences(teacher.getLikeTime());
                    if (!preferences.isEmpty()) {
                        teacherTimePreferences.put(teacherUuid, preferences);
                    }
                }
            }
        }
        context.setTeacherTimePreferences(teacherTimePreferences);
        context.setTeacherMaxHours(teacherMaxHours);

        // 5. 查询已有的正式排课记录（status=1，用于冲突检测）
        List<ScheduleDO> existingSchedules = scheduleDAO.list(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", params.getSemesterUuid())
                        .eq("status", 1)
        );

        // 转换为 ExistingSchedule 格式
        List<ScheduleContext.ExistingSchedule> existingScheduleList = existingSchedules.stream()
                .map(sc -> {
                    ScheduleContext.ExistingSchedule es = new ScheduleContext.ExistingSchedule();
                    es.setScheduleUuid(sc.getScheduleUuid());
                    es.setTeachingClassUuid(sc.getTeachingClassUuid());
                    es.setTeacherUuid(sc.getTeacherUuid());
                    es.setClassroomUuid(sc.getClassroomUuid());

                    // 查询关联的行政班
                    List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(
                            new QueryWrapper<TeachingClassClassDO>()
                                    .eq("teaching_class_uuid", sc.getTeachingClassUuid())
                    );
                    es.setClassUuids(tccList.stream()
                            .map(TeachingClassClassDO::getClassUuid)
                            .collect(Collectors.toList()));

                    // 构建时间槽
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setDayOfWeek(sc.getDayOfWeek());
                    timeSlot.setSectionStart(sc.getSectionStart());
                    timeSlot.setSectionEnd(sc.getSectionEnd());
                    timeSlot.setWeeks(parseWeeksJson(sc.getWeeksJson()));
                    es.setTimeSlot(timeSlot);

                    return es;
                })
                .collect(Collectors.toList());
        context.setExistingSchedules(existingScheduleList);

        // 6. 设置固定参数
        context.setSectionsPerDay(12); // 每天12节（6个2节时段）
        context.setDaysPerWeek(5);      // 每周5天
        context.setHoursPerSession(2);  // 单次2节

        // 7. 传递课程-行政班映射（用于合班约束）
        context.setCourseClassMapping(params.getCourseClassMapping());

        return context;
    }

    /**
     * 解析教师时间偏好字符串
     * 格式: "1-1-2,1-3-4" 表示周一第1-2节和3-4节偏好
     */
    private List<TimeSlot> parseTeacherPreferences(String preferenceStr) {
        List<TimeSlot> preferences = new ArrayList<>();
        if (preferenceStr == null || preferenceStr.isEmpty()) {
            return preferences;
        }

        try {
            String[] parts = preferenceStr.split(",");
            for (String part : parts) {
                String[] slotParts = part.trim().split("-");
                if (slotParts.length >= 3) {
                    TimeSlot slot = new TimeSlot();
                    slot.setDayOfWeek(Integer.parseInt(slotParts[0]));
                    slot.setSectionStart(Integer.parseInt(slotParts[1]));
                    slot.setSectionEnd(Integer.parseInt(slotParts[2]));
                    slot.setWeeks(new ArrayList<>()); // 偏好不限制周次
                    preferences.add(slot);
                }
            }
        } catch (Exception e) {
            log.warn("解析教师时间偏好失败: {}", preferenceStr, e);
        }

        return preferences;
    }

    /**
     * 解析周次JSON字符串
     */
    private List<Integer> parseWeeksJson(String weeksJson) {
        if (weeksJson == null || weeksJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(weeksJson, new TypeReference<List<Integer>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("解析周次JSON失败: {}", weeksJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存排课记录到数据库
     */
    private void saveScheduleRecords(AutoScheduleResult result, Integer scheduleMode, ScheduleContext context) {
        if (result.getScheduleMap() == null || result.getScheduleMap().isEmpty()) {
            log.warn("没有需要保存的排课记录");
            return;
        }

        List<ScheduleDO> scheduleRecords = new ArrayList<>();

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : result.getScheduleMap().entrySet()) {
            TimeSlot timeSlot = entry.getKey();
            List<CourseAppointment> appointments = entry.getValue();

            for (CourseAppointment appt : appointments) {
                ScheduleDO schedule = new ScheduleDO();
                schedule.setScheduleUuid(UUID.randomUUID().toString().replace("-", ""));
                schedule.setSemesterUuid(result.getSemesterUuid());
                schedule.setTeachingClassUuid(appt.getTeachingClassUuid());
                schedule.setCourseUuid(appt.getCourseUuid());
                schedule.setTeacherUuid(appt.getTeacherUuid());
                schedule.setClassroomUuid(appt.getClassroomUuid());
                schedule.setDayOfWeek(timeSlot.getDayOfWeek());
                schedule.setSectionStart(timeSlot.getSectionStart());
                schedule.setSectionEnd(timeSlot.getSectionEnd());

                // 将周次列表转换为JSON字符串
                try {
                    schedule.setWeeksJson(objectMapper.writeValueAsString(timeSlot.getWeeks()));
                } catch (JsonProcessingException e) {
                    log.error("序列化周次JSON失败", e);
                    schedule.setWeeksJson("[]");
                }

                schedule.setCreditHours(timeSlot.getTotalHours());
                schedule.setUpdatedAt(LocalDateTime.now());

                // 默认预览模式(0)，如果指定了scheduleMode则使用指定值
                schedule.setStatus(scheduleMode != null ? scheduleMode : 0);

                scheduleRecords.add(schedule);
            }
        }

        // 批量保存
        if (!scheduleRecords.isEmpty()) {
            scheduleDAO.saveBatch(scheduleRecords);
            log.info("保存了 {} 条排课记录", scheduleRecords.size());
        }
    }

    /**
     * 保存排课方案为预览状态
     *
     * @param semesterUuid 学期UUID
     * @param result       排课结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAsPreview(String semesterUuid, AutoScheduleResult result) {
        log.info("保存预览方案，学期UUID: {}", semesterUuid);

        // 删除该学期的旧预览记录（status=0）
        scheduleDAO.remove(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
        );

        // 保存新的预览记录（status=0）
        saveScheduleRecords(result, 0, null);

        log.info("预览方案保存完成");
    }

    /**
     * 确认排课方案（将预览状态转为正式状态）
     *
     * @param semesterUuid 学期UUID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSchedule(String semesterUuid) {
        log.info("确认排课方案，学期UUID: {}", semesterUuid);

        // 将预览记录（status=0）转为正式记录（status=1）
        scheduleDAO.update(
                new UpdateWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
                        .set("status", 1)
                        .set("updated_at", LocalDateTime.now())
        );

        log.info("排课方案确认完成");
    }

    /**
     * 清除预览排课方案
     *
     * @param semesterUuid 学期UUID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearPreview(String semesterUuid) {
        log.info("清除预览方案，学期UUID: {}", semesterUuid);

        // 删除预览记录（status=0）
        boolean deleted = scheduleDAO.remove(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
        );

        log.info("预览方案清除完成，删除结果: {}", deleted);
    }

    /**
     * 获取排课统计信息
     *
     * @param semesterUuid 学期UUID
     * @return 统计信息
     */
    @Override
    public AutoScheduleResult.ScheduleStatistics getStatistics(String semesterUuid) {
        AutoScheduleResult.ScheduleStatistics statistics = new AutoScheduleResult.ScheduleStatistics();

        // 统计该学期的排课记录（status=1，正式记录）
        List<ScheduleDO> schedules = scheduleDAO.list(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 1)
        );

        statistics.setTotalSessions(schedules.size());
        statistics.setTotalHours(schedules.stream().mapToInt(ScheduleDO::getCreditHours).sum());

        // 统计已排课的教学班数量
        Set<String> scheduledTeachingClasses = schedules.stream()
                .map(ScheduleDO::getTeachingClassUuid)
                .collect(Collectors.toSet());
        statistics.setScheduledTeachingClasses(scheduledTeachingClasses.size());

        // 查询该学期的总教学班数量
        int totalTeachingClasses = (int) teachingClassDAO.count(
                new QueryWrapper<TeachingClassDO>()
                        .eq("semester_uuid", semesterUuid)
        );
        statistics.setTotalTeachingClasses(totalTeachingClasses);

        // 计算平均适应度（从无冲突比例估算）
        double averageFitness = 0.0;
        if (totalTeachingClasses > 0) {
            averageFitness = (double) scheduledTeachingClasses.size() / totalTeachingClasses * 1000;
        }
        statistics.setAverageFitness(averageFitness);

        return statistics;
    }

    // ==================== 按行政班级排课新方法 ====================

    /**
     * 解析并创建教学班（按行政班级排课模式）
     *
     * @param params 排课参数
     * @return 教学班UUID列表
     */
    private List<String> parseAndCreateTeachingClasses(AutoScheduleVO params) {
        List<String> teachingClassUuids = new ArrayList<>();
        String semesterUuid = params.getSemesterUuid();

        for (Map.Entry<String, List<String>> entry : params.getCourseClassMapping().entrySet()) {
            String courseUuid = entry.getKey();
            List<String> classUuids = entry.getValue();

            // 1. 选择教师
            String teacherUuid = selectTeacherForCourse(courseUuid, params);

            if (teacherUuid == null) {
                throw new IllegalArgumentException("课程 " + courseUuid + " 没有可用的有资格教师");
            }

            // 2. 查找或创建教学班
            String teachingClassUuid = findOrBuildTeachingClass(courseUuid, teacherUuid, semesterUuid, classUuids);
            teachingClassUuids.add(teachingClassUuid);

            log.info("课程: {}, 教师: {}, 行政班: {} -> 教学班: {}",
                    courseUuid, teacherUuid, classUuids, teachingClassUuid);
        }

        return teachingClassUuids;
    }

    /**
     * 为课程选择教师
     *
     * @param courseUuid 课程UUID
     * @param params 排课参数
     * @return 教师UUID
     */
    private String selectTeacherForCourse(String courseUuid, AutoScheduleVO params) {
        // 1. 如果指定了教师，验证后返回
        if (params.getTeacherAssignment() != null && params.getTeacherAssignment().containsKey(courseUuid)) {
            String assignedTeacherUuid = params.getTeacherAssignment().get(courseUuid);
            if (hasQualification(courseUuid, assignedTeacherUuid)) {
                return assignedTeacherUuid;
            } else {
                throw new IllegalArgumentException("指定的教师 " + assignedTeacherUuid + " 没有课程 " + courseUuid + " 的授课资格");
            }
        }

        // 2. 查询所有有资格的教师
        List<CourseQualificationDO> qualifications = courseQualificationDAO.list(
                new QueryWrapper<CourseQualificationDO>().eq("course_uuid", courseUuid)
        );

        if (qualifications.isEmpty()) {
            return null;
        }

        List<String> qualifiedTeacherUuids = qualifications.stream()
                .map(CourseQualificationDO::getTeacherUuid)
                .collect(Collectors.toList());

        // 3. 根据策略选择教师
        String strategy = params.getTeacherSelectionStrategy();
        if ("random".equals(strategy)) {
            return selectRandomTeacher(qualifiedTeacherUuids);
        } else if ("first".equals(strategy)) {
            return qualifiedTeacherUuids.get(0);
        } else {
            // balanced (默认)
            return selectBalancedTeacher(qualifiedTeacherUuids, params.getSemesterUuid());
        }
    }

    /**
     * 检查教师是否有课程授课资格
     */
    private boolean hasQualification(String courseUuid, String teacherUuid) {
        Long count = courseQualificationDAO.count(
                new QueryWrapper<CourseQualificationDO>()
                        .eq("course_uuid", courseUuid)
                        .eq("teacher_uuid", teacherUuid)
        );
        return count != null && count > 0;
    }

    /**
     * 随机选择教师
     */
    private String selectRandomTeacher(List<String> teacherUuids) {
        Random random = new Random();
        return teacherUuids.get(random.nextInt(teacherUuids.size()));
    }

    /**
     * 均衡选择教师（选择当前工作量最少的）
     */
    private String selectBalancedTeacher(List<String> teacherUuids, String semesterUuid) {
        String selectedTeacher = null;
        int minWorkload = Integer.MAX_VALUE;

        // 查询每个教师当前的工作量
        for (String teacherUuid : teacherUuids) {
            // 统计该教师在当前学期的总课时
            List<ScheduleDO> schedules = scheduleDAO.list(
                    new QueryWrapper<ScheduleDO>()
                            .eq("semester_uuid", semesterUuid)
                            .eq("teacher_uuid", teacherUuid)
                            .eq("status", 1) // 只统计正式排课
            );

            int workload = schedules.stream().mapToInt(ScheduleDO::getCreditHours).sum();

            if (workload < minWorkload) {
                minWorkload = workload;
                selectedTeacher = teacherUuid;
            }
        }

        return selectedTeacher;
    }

    /**
     * 查找或构建教学班
     *
     * @param courseUuid 课程UUID
     * @param teacherUuid 教师UUID
     * @param semesterUuid 学期UUID
     * @param classUuids 行政班UUID列表
     * @return 教学班UUID
     */
    private String findOrBuildTeachingClass(String courseUuid, String teacherUuid,
                                              String semesterUuid, List<String> classUuids) {
        // 1. 排序行政班UUID列表，便于比较
        List<String> sortedClassUuids = new ArrayList<>(classUuids);
        Collections.sort(sortedClassUuids);

        // 2. 查询匹配的教学班
        // 条件：相同课程 + 相同教师 + 相同学期 + 相同行政班组合
        List<TeachingClassDO> existingClasses = teachingClassDAO.list(
                new QueryWrapper<TeachingClassDO>()
                        .eq("course_uuid", courseUuid)
                        .eq("teacher_uuid", teacherUuid)
                        .eq("semester_uuid", semesterUuid)
        );

        // 3. 检查每个现有教学班的行政班组合是否匹配
        for (TeachingClassDO tc : existingClasses) {
            List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(
                    new QueryWrapper<TeachingClassClassDO>()
                            .eq("teaching_class_uuid", tc.getTeachingClassUuid())
            );

            List<String> existingClassUuids = tccList.stream()
                    .map(TeachingClassClassDO::getClassUuid)
                    .sorted()
                    .collect(Collectors.toList());

            if (existingClassUuids.equals(sortedClassUuids)) {
                log.info("复用已有教学班: {}", tc.getTeachingClassUuid());
                return tc.getTeachingClassUuid();
            }
        }

        // 4. 未找到匹配的教学班，创建新的
        return createNewTeachingClass(courseUuid, teacherUuid, semesterUuid, classUuids);
    }

    /**
     * 创建新的教学班
     *
     * @param courseUuid 课程UUID
     * @param teacherUuid 教师UUID
     * @param semesterUuid 学期UUID
     * @param classUuids 行政班UUID列表
     * @return 教学班UUID
     */
    private String createNewTeachingClass(String courseUuid, String teacherUuid,
                                          String semesterUuid, List<String> classUuids) {
        // 1. 查询课程信息
        CourseDO course = courseDAO.getById(courseUuid);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseUuid);
        }

        // 2. 查询教师信息
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        if (teacher == null) {
            throw new IllegalArgumentException("教师不存在: " + teacherUuid);
        }

        // 3. 查询行政班信息
        List<ClassDO> classes = classDAO.listByIds(classUuids);
        if (classes.size() != classUuids.size()) {
            throw new IllegalArgumentException("部分行政班不存在");
        }

        // 4. 构建教学班名称：课程名-教师名-行政班1+...
        String classNameStr = classes.stream()
                .map(ClassDO::getClassName)
                .collect(Collectors.joining("+"));
        String teachingClassName = course.getCourseName() + "-" + teacher.getTeacherName() + "-" + classNameStr;

        // 5. 创建教学班
        TeachingClassDO teachingClass = new TeachingClassDO();
        teachingClass.setTeachingClassUuid(UUID.randomUUID().toString().replace("-", ""));
        teachingClass.setCourseUuid(courseUuid);
        teachingClass.setTeacherUuid(teacherUuid);
        teachingClass.setSemesterUuid(semesterUuid);
        teachingClass.setTeachingClassName(teachingClassName);
        teachingClass.setTeachingClassHours(0);
        teachingClass.setWeeklySessions(null); // 使用课程默认值
        teachingClass.setSectionsPerSession(null); // 使用默认值2

        teachingClassDAO.save(teachingClass);
        log.info("创建教学班: {}", teachingClassName);

        // 6. 批量创建教学班-行政班关联
        List<TeachingClassClassDO> tccList = new ArrayList<>();
        for (String classUuid : classUuids) {
            TeachingClassClassDO tcc = new TeachingClassClassDO();
            tcc.setTeachingClassClassUuid(UUID.randomUUID().toString().replace("-", ""));
            tcc.setTeachingClassUuid(teachingClass.getTeachingClassUuid());
            tcc.setClassUuid(classUuid);
            tccList.add(tcc);
        }
        teachingClassClassDAO.saveBatch(tccList);
        log.info("创建教学班-行政班关联: {} 条", tccList.size());

        return teachingClass.getTeachingClassUuid();
    }
}
