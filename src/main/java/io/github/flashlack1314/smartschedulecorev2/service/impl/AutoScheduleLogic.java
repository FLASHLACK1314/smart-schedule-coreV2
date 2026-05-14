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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private final ScheduleConflictDAO scheduleConflictDAO;
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
     * 排课记录与课程安排的组合类
     * 用于在冲突检测时保留ScheduleDO引用以获取scheduleUuid
     */
    private static class ScheduleAppointment {
        private final ScheduleDO scheduleDO;
        private final CourseAppointment appointment;

        public ScheduleAppointment(ScheduleDO scheduleDO, CourseAppointment appointment) {
            this.scheduleDO = scheduleDO;
            this.appointment = appointment;
        }

        public ScheduleDO getScheduleDO() {
            return scheduleDO;
        }

        public CourseAppointment getAppointment() {
            return appointment;
        }
    }

    /**
     * 执行自动排课
     *
     * @param params 排课参数
     * @return 排课结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoScheduleResult execute(AutoScheduleVO params) {
        return executeWithSse(params, null);
    }

    /**
     * 执行自动排课（SSE版本，保持连接直到完成）
     *
     * @param params 排课参数
     * @param emitter SSE emitter
     * @return 排课结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoScheduleResult executeWithSse(AutoScheduleVO params, SseEmitter emitter) {
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

        // 5. 执行遗传算法（传入SSE emitter）
        Chromosome bestChromosome = geneticAlgorithm.schedule(emitter);

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

            // 计算需要的上课次数和周数
            int hoursPerSession = 2; // 固定2节
            // 使用教学班默认值
            int weeklySessions = (tc.getWeeklySessions() != null ? tc.getWeeklySessions() : 1);
            int sectionsPerSession = (tc.getSectionsPerSession() != null ? tc.getSectionsPerSession() : 2);

            // 新逻辑：requiredSessions = 每周上课次数（而非总学时/2）
            int requiredSessions = weeklySessions;

            // 计算需要的周数：ceil(总学时 / (每周次数 × 每次学时))
            int requiredWeeks = hoursCalculator.calculateRequiredWeeks(
                    course.getCourseHours(), weeklySessions, hoursPerSession);

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
            info.setRequiredWeeks(requiredWeeks);

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

        // 3.2 查询课程-教师资格映射关系
        List<CourseQualificationDO> courseQualifications = courseQualificationDAO.list();
        Map<String, List<String>> courseTeacherQualifications = courseQualifications.stream()
                .collect(Collectors.groupingBy(
                        CourseQualificationDO::getCourseUuid,
                        Collectors.mapping(
                                CourseQualificationDO::getTeacherUuid,
                                Collectors.toList()
                        )
                ));
        context.setCourseTeacherQualifications(courseTeacherQualifications);
        log.info("加载课程-教师资格映射: {} 条", courseQualifications.size());

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
    public ConfirmResult confirmSchedule(String semesterUuid) {
        log.info("确认排课方案，学期UUID: {}", semesterUuid);

        // 检测并保存冲突
        int conflictCount = detectAndSaveConflicts(semesterUuid);
        log.info("检测到 {} 个冲突记录", conflictCount);

        // 将预览记录（status=0）转为正式记录（status=1）
        scheduleDAO.update(
                new UpdateWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
                        .set("status", 1)
                        .set("updated_at", LocalDateTime.now())
        );

        String message = conflictCount > 0
                ? "确认成功，但检测到 " + conflictCount + " 个冲突"
                : "确认排课方案成功";
        log.info("排课方案确认完成: {}", message);

        return new ConfirmResult(conflictCount, message);
    }

    /**
     * 检测并保存排课冲突
     *
     * @param semesterUuid 学期UUID
     * @return 检测到的冲突数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int detectAndSaveConflicts(String semesterUuid) {
        log.info("检测并保存排课冲突，学期UUID: {}", semesterUuid);

        // 1. 删除该学期的旧冲突记录（仅针对预览状态的排课）
        List<ScheduleDO> previewScheduleList = scheduleDAO.list(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
        );
        List<String> previewScheduleUuids = previewScheduleList.stream()
                .map(ScheduleDO::getScheduleUuid)
                .collect(Collectors.toList());

        if (!previewScheduleUuids.isEmpty()) {
            scheduleConflictDAO.remove(
                    new QueryWrapper<ScheduleConflictDO>()
                            .eq("semester_uuid", semesterUuid)
                            .in("schedule_uuid_a", previewScheduleUuids)
            );
        }

        // 2. 查询预览状态的排课记录（status=0）
        List<ScheduleDO> previewSchedules = scheduleDAO.list(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 0)
        );

        if (previewSchedules.isEmpty()) {
            log.info("没有预览状态的排课记录，无需检测冲突");
            return 0;
        }

        // 3. 查询已有正式排课记录（status=1）用于冲突检测
        List<ScheduleDO> existingSchedules = scheduleDAO.list(
                new QueryWrapper<ScheduleDO>()
                        .eq("semester_uuid", semesterUuid)
                        .eq("status", 1)
        );

        // 4. 构建教学班信息映射
        Map<String, TeachingClassDO> teachingClassMap = teachingClassDAO.listByIds(
                previewSchedules.stream().map(ScheduleDO::getTeachingClassUuid).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(TeachingClassDO::getTeachingClassUuid, tc -> tc));

        Map<String, CourseDO> courseMap = courseDAO.listByIds(
                previewSchedules.stream().map(ScheduleDO::getCourseUuid).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(CourseDO::getCourseUuid, c -> c));

        Map<String, TeacherDO> teacherMap = teacherDAO.listByIds(
                previewSchedules.stream().map(ScheduleDO::getTeacherUuid).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(TeacherDO::getTeacherUuid, t -> t));

        Map<String, ClassroomDO> classroomMap = classroomDAO.listByIds(
                previewSchedules.stream().map(ScheduleDO::getClassroomUuid).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(ClassroomDO::getClassroomUuid, cr -> cr));

        // 5. 构建行政班信息映射
        Map<String, List<String>> scheduleClassUuidsMap = new HashMap<>();
        for (ScheduleDO schedule : previewSchedules) {
            List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(
                    new QueryWrapper<TeachingClassClassDO>()
                            .eq("teaching_class_uuid", schedule.getTeachingClassUuid())
            );
            scheduleClassUuidsMap.put(schedule.getScheduleUuid(),
                    tccList.stream().map(TeachingClassClassDO::getClassUuid).collect(Collectors.toList()));
        }

        // 6. 转换预览记录为 CourseAppointment 格式（同时保留ScheduleDO引用）
        List<ScheduleAppointment> previewAppointmentList = previewSchedules.stream().map(sc -> {
            CourseAppointment appt = new CourseAppointment();
            appt.setTeachingClassUuid(sc.getTeachingClassUuid());
            appt.setCourseUuid(sc.getCourseUuid());
            appt.setTeacherUuid(sc.getTeacherUuid());
            appt.setClassroomUuid(sc.getClassroomUuid());

            TeachingClassDO tc = teachingClassMap.get(sc.getTeachingClassUuid());
            if (tc != null) {
                appt.setTeachingClassName(tc.getTeachingClassName());
            }
            CourseDO course = courseMap.get(sc.getCourseUuid());
            if (course != null) {
                appt.setCourseName(course.getCourseName());
                appt.setCourseTypeUuid(course.getCourseTypeUuid());
            }
            TeacherDO teacher = teacherMap.get(sc.getTeacherUuid());
            if (teacher != null) {
                appt.setTeacherName(teacher.getTeacherName());
            }
            ClassroomDO classroom = classroomMap.get(sc.getClassroomUuid());
            if (classroom != null) {
                appt.setClassroomName(classroom.getClassroomName());
                appt.setClassroomCapacity(classroom.getClassroomCapacity());
                appt.setClassroomTypeUuid(classroom.getClassroomTypeUuid());
            }

            appt.setClassUuids(scheduleClassUuidsMap.get(sc.getScheduleUuid()));

            // 构建时间槽
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setDayOfWeek(sc.getDayOfWeek());
            timeSlot.setSectionStart(sc.getSectionStart());
            timeSlot.setSectionEnd(sc.getSectionEnd());
            timeSlot.setWeeks(parseWeeksJson(sc.getWeeksJson()));
            appt.setTimeSlot(timeSlot);

            return new ScheduleAppointment(sc, appt);
        }).collect(Collectors.toList());

        // 提取仅包含CourseAppointment的列表供后续检测使用
        List<CourseAppointment> previewAppointments = previewAppointmentList.stream()
                .map(ScheduleAppointment::getAppointment)
                .collect(Collectors.toList());

        // 7. 转换正式记录为 ExistingSchedule 格式
        List<ScheduleContext.ExistingSchedule> existingScheduleList = existingSchedules.stream().map(sc -> {
            ScheduleContext.ExistingSchedule es = new ScheduleContext.ExistingSchedule();
            es.setScheduleUuid(sc.getScheduleUuid());
            es.setTeachingClassUuid(sc.getTeachingClassUuid());
            es.setTeacherUuid(sc.getTeacherUuid());
            es.setClassroomUuid(sc.getClassroomUuid());

            List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(
                    new QueryWrapper<TeachingClassClassDO>()
                            .eq("teaching_class_uuid", sc.getTeachingClassUuid())
            );
            es.setClassUuids(tccList.stream().map(TeachingClassClassDO::getClassUuid).collect(Collectors.toList()));

            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setDayOfWeek(sc.getDayOfWeek());
            timeSlot.setSectionStart(sc.getSectionStart());
            timeSlot.setSectionEnd(sc.getSectionEnd());
            timeSlot.setWeeks(parseWeeksJson(sc.getWeeksJson()));
            es.setTimeSlot(timeSlot);

            return es;
        }).collect(Collectors.toList());

        // 8. 构建最小上下文（仅用于冲突检测）
        ScheduleContext context = new ScheduleContext();
        context.setSemesterUuid(semesterUuid);
        context.setExistingSchedules(existingScheduleList);

        // 查询课程类型-教室类型映射关系
        List<CourseClassroomTypeDO> courseClassroomTypes = courseClassroomTypeDAO.list();
        Map<String, List<String>> courseTypeToClassroomTypes = courseClassroomTypes.stream()
                .collect(Collectors.groupingBy(
                        CourseClassroomTypeDO::getCourseTypeUuid,
                        Collectors.mapping(CourseClassroomTypeDO::getClassroomTypeUuid, Collectors.toList())
                ));
        context.setCourseTypeToClassroomTypes(courseTypeToClassroomTypes);

        // 查询课程-教师资格映射关系
        List<CourseQualificationDO> courseQualifications = courseQualificationDAO.list();
        Map<String, List<String>> courseTeacherQualifications = courseQualifications.stream()
                .collect(Collectors.groupingBy(
                        CourseQualificationDO::getCourseUuid,
                        Collectors.mapping(CourseQualificationDO::getTeacherUuid, Collectors.toList())
                ));
        context.setCourseTeacherQualifications(courseTeacherQualifications);

        // 9. 检测预览记录之间的冲突（内部冲突）
        int conflictAdded = 0;

        // 检测两两冲突
        for (int i = 0; i < previewAppointmentList.size(); i++) {
            for (int j = i + 1; j < previewAppointmentList.size(); j++) {
                ScheduleAppointment sa1 = previewAppointmentList.get(i);
                ScheduleAppointment sa2 = previewAppointmentList.get(j);
                CourseAppointment appt1 = sa1.getAppointment();
                CourseAppointment appt2 = sa2.getAppointment();

                // 跳过同一教学班的不同次排课
                if (appt1.getTeachingClassUuid().equals(appt2.getTeachingClassUuid())) {
                    continue;
                }

                TimeSlot slot1 = appt1.getTimeSlot();
                TimeSlot slot2 = appt2.getTimeSlot();

                if (slot1.isOverlap(slot2)) {
                    // 教师冲突
                    if (appt1.getTeacherUuid().equals(appt2.getTeacherUuid())) {
                        addScheduleConflict(semesterUuid, sa1.getScheduleDO().getScheduleUuid(),
                                sa2.getScheduleDO().getScheduleUuid(), "TEACHER_TIME_CONFLICT",
                                "教师[" + appt1.getTeacherName() + "]同一时间有多门课程", 1);
                        conflictAdded++;
                    }

                    // 教室冲突
                    if (appt1.getClassroomUuid().equals(appt2.getClassroomUuid())) {
                        addScheduleConflict(semesterUuid, sa1.getScheduleDO().getScheduleUuid(),
                                sa2.getScheduleDO().getScheduleUuid(), "CLASSROOM_TIME_CONFLICT",
                                "教室[" + appt1.getClassroomName() + "]同一时间被多个课程占用", 1);
                        conflictAdded++;
                    }

                    // 班级冲突
                    if (appt1.getClassUuids() != null && appt2.getClassUuids() != null) {
                        Set<String> classes1 = new HashSet<>(appt1.getClassUuids());
                        Set<String> classes2 = new HashSet<>(appt2.getClassUuids());
                        classes1.retainAll(classes2);

                        if (!classes1.isEmpty()) {
                            addScheduleConflict(semesterUuid, sa1.getScheduleDO().getScheduleUuid(),
                                    sa2.getScheduleDO().getScheduleUuid(), "CLASS_TIME_CONFLICT",
                                    "班级同一时间有多门课程", 1);
                            conflictAdded++;
                        }
                    }
                }
            }
        }

        // 10. 检测预览记录与已有正式记录的冲突
        for (ScheduleAppointment sa : previewAppointmentList) {
            CourseAppointment newAppt = sa.getAppointment();
            String newScheduleUuid = sa.getScheduleDO().getScheduleUuid();
            for (ScheduleContext.ExistingSchedule existing : existingScheduleList) {
                if (newAppt.getTimeSlot().isOverlap(existing.getTimeSlot())) {
                    // 教师冲突
                    if (newAppt.getTeacherUuid().equals(existing.getTeacherUuid())) {
                        ScheduleDO existingSchedule = existingSchedules.stream()
                                .filter(sc -> sc.getScheduleUuid().equals(existing.getScheduleUuid()))
                                .findFirst().orElse(null);
                        if (existingSchedule != null) {
                            addScheduleConflictWithExisting(semesterUuid, newScheduleUuid,
                                    existingSchedule.getScheduleUuid(), "EXISTING_SCHEDULE_CONFLICT",
                                    "教师[" + newAppt.getTeacherName() + "]在已有排课中该时间段有课", 1);
                            conflictAdded++;
                        }
                    }

                    // 教室冲突
                    if (newAppt.getClassroomUuid().equals(existing.getClassroomUuid())) {
                        ScheduleDO existingSchedule = existingSchedules.stream()
                                .filter(sc -> sc.getScheduleUuid().equals(existing.getScheduleUuid()))
                                .findFirst().orElse(null);
                        if (existingSchedule != null) {
                            addScheduleConflictWithExisting(semesterUuid, newScheduleUuid,
                                    existingSchedule.getScheduleUuid(), "EXISTING_SCHEDULE_CONFLICT",
                                    "教室[" + newAppt.getClassroomName() + "]在已有排课中该时间段已被占用", 1);
                            conflictAdded++;
                        }
                    }

                    // 班级冲突
                    if (newAppt.getClassUuids() != null && existing.getClassUuids() != null) {
                        Set<String> newClasses = new HashSet<>(newAppt.getClassUuids());
                        Set<String> existingClasses = new HashSet<>(existing.getClassUuids());
                        newClasses.retainAll(existingClasses);

                        if (!newClasses.isEmpty()) {
                            ScheduleDO existingSchedule = existingSchedules.stream()
                                    .filter(sc -> sc.getScheduleUuid().equals(existing.getScheduleUuid()))
                                    .findFirst().orElse(null);
                            if (existingSchedule != null) {
                                addScheduleConflictWithExisting(semesterUuid, newScheduleUuid,
                                        existingSchedule.getScheduleUuid(), "EXISTING_SCHEDULE_CONFLICT",
                                        "班级在已有排课中该时间段有课", 1);
                                conflictAdded++;
                            }
                        }
                    }
                }
            }
        }

        // 11. 检测容量约束
        for (ScheduleAppointment sa : previewAppointmentList) {
            CourseAppointment appt = sa.getAppointment();
            if (appt.getClassroomCapacity() != null && appt.getTotalStudents() != null) {
                if (appt.getClassroomCapacity() < appt.getTotalStudents()) {
                    // 容量不足的冲突也需要记录，但这种是单个排课的问题
                    ScheduleConflictDO conflict = new ScheduleConflictDO();
                    conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
                    conflict.setSemesterUuid(semesterUuid);
                    conflict.setScheduleUuidA(sa.getScheduleDO().getScheduleUuid());
                    conflict.setScheduleUuidB(null);
                    conflict.setConflictType("CAPACITY_INSUFFICIENT");
                    conflict.setSeverity(1);
                    conflict.setDescription("教室[" + appt.getClassroomName() + "]容量不足，需要" + appt.getTotalStudents() + "个座位，实际只有" + appt.getClassroomCapacity() + "个");
                    scheduleConflictDAO.save(conflict);
                    conflictAdded++;
                }
            }
        }

        // 12. 检测教室类型匹配约束
        Map<String, List<String>> courseTypeClassroomMap = context.getCourseTypeToClassroomTypes();
        if (courseTypeClassroomMap != null && !courseTypeClassroomMap.isEmpty()) {
            for (ScheduleAppointment sa : previewAppointmentList) {
                CourseAppointment appt = sa.getAppointment();
                String courseTypeUuid = appt.getCourseTypeUuid();
                String classroomTypeUuid = appt.getClassroomTypeUuid();

                if (courseTypeUuid != null && classroomTypeUuid != null) {
                    List<String> allowedTypes = courseTypeClassroomMap.get(courseTypeUuid);
                    if (allowedTypes != null && !allowedTypes.isEmpty() && !allowedTypes.contains(classroomTypeUuid)) {
                        ScheduleConflictDO conflict = new ScheduleConflictDO();
                        conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
                        conflict.setSemesterUuid(semesterUuid);
                        conflict.setScheduleUuidA(sa.getScheduleDO().getScheduleUuid());
                        conflict.setScheduleUuidB(null);
                        conflict.setConflictType("CLASSROOM_TYPE_MISMATCH");
                        conflict.setSeverity(1);
                        conflict.setDescription("课程[" + appt.getCourseName() + "]需要类型为" + allowedTypes + "的教室，但被安排到了类型不匹配的教室[" + appt.getClassroomName() + "]");
                        scheduleConflictDAO.save(conflict);
                        conflictAdded++;
                    }
                }
            }
        }

        // 13. 检测教师资格约束
        Map<String, List<String>> courseTeacherQualMap = context.getCourseTeacherQualifications();
        if (courseTeacherQualMap != null && !courseTeacherQualMap.isEmpty()) {
            for (ScheduleAppointment sa : previewAppointmentList) {
                CourseAppointment appt = sa.getAppointment();
                String courseUuid = appt.getCourseUuid();
                String teacherUuid = appt.getTeacherUuid();

                if (courseUuid != null && teacherUuid != null) {
                    List<String> qualifiedTeachers = courseTeacherQualMap.get(courseUuid);

                    if (qualifiedTeachers == null || qualifiedTeachers.isEmpty()) {
                        ScheduleConflictDO conflict = new ScheduleConflictDO();
                        conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
                        conflict.setSemesterUuid(semesterUuid);
                        conflict.setScheduleUuidA(sa.getScheduleDO().getScheduleUuid());
                        conflict.setScheduleUuidB(null);
                        conflict.setConflictType("TEACHER_QUALIFICATION_MISMATCH");
                        conflict.setSeverity(1);
                        conflict.setDescription("课程[" + appt.getCourseName() + "]没有任何有资格教师，却安排了教师[" + appt.getTeacherName() + "]");
                        scheduleConflictDAO.save(conflict);
                        conflictAdded++;
                    } else if (!qualifiedTeachers.contains(teacherUuid)) {
                        ScheduleConflictDO conflict = new ScheduleConflictDO();
                        conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
                        conflict.setSemesterUuid(semesterUuid);
                        conflict.setScheduleUuidA(sa.getScheduleDO().getScheduleUuid());
                        conflict.setScheduleUuidB(null);
                        conflict.setConflictType("TEACHER_QUALIFICATION_MISMATCH");
                        conflict.setSeverity(1);
                        conflict.setDescription("教师[" + appt.getTeacherName() + "]没有教授课程[" + appt.getCourseName() + "]的资格");
                        scheduleConflictDAO.save(conflict);
                        conflictAdded++;
                    }
                }
            }
        }

        log.info("检测并保存排课冲突完成，共添加 {} 条冲突记录", conflictAdded);
        return conflictAdded;
    }

    /**
     * 添加排课冲突记录（两个新排课之间的冲突）
     */
    private void addScheduleConflict(String semesterUuid, String scheduleUuidA, String scheduleUuidB,
                                      String conflictType, String description, Integer severity) {
        ScheduleConflictDO conflict = new ScheduleConflictDO();
        conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
        conflict.setSemesterUuid(semesterUuid);
        conflict.setScheduleUuidA(scheduleUuidA);
        conflict.setScheduleUuidB(scheduleUuidB);
        conflict.setConflictType(conflictType);
        conflict.setSeverity(severity);
        conflict.setDescription(description);
        scheduleConflictDAO.save(conflict);
    }

    /**
     * 添加排课冲突记录（预览排课与已有正式排课的冲突）
     */
    private void addScheduleConflictWithExisting(String semesterUuid, String newScheduleUuid,
                                                  String existingScheduleUuid, String conflictType,
                                                  String description, Integer severity) {
        ScheduleConflictDO conflict = new ScheduleConflictDO();
        conflict.setConflictUuid(UUID.randomUUID().toString().replace("-", ""));
        conflict.setSemesterUuid(semesterUuid);
        conflict.setScheduleUuidA(newScheduleUuid);
        conflict.setScheduleUuidB(existingScheduleUuid);
        conflict.setConflictType(conflictType);
        conflict.setSeverity(severity);
        conflict.setDescription(description);
        scheduleConflictDAO.save(conflict);
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
     * 教学班最大容量（默认50人）
     */
    private static final int MAX_TEACHING_CLASS_CAPACITY = 50;

    /**
     * 解析并创建教学班（按行政班级排课模式，按容量自动拆分）
     *
     * @param params 排课参数
     * @return 教学班UUID列表
     */
    private List<String> parseAndCreateTeachingClasses(AutoScheduleVO params) {
        List<String> teachingClassUuids = new ArrayList<>();
        String semesterUuid = params.getSemesterUuid();

        log.info("开始解析并创建教学班，courseClassMapping: {}", params.getCourseClassMapping());

        for (Map.Entry<String, List<String>> entry : params.getCourseClassMapping().entrySet()) {
            String courseUuid = entry.getKey();
            List<String> classUuids = entry.getValue();

            // 1. 选择教师
            String teacherUuid = selectTeacherForCourse(courseUuid, params);

            if (teacherUuid == null) {
                throw new IllegalArgumentException("课程 " + courseUuid + " 没有可用的有资格教师");
            }

            // 2. 按容量拆分行政班，获取拆分后的分组
            List<List<String>> splitClassGroups = splitClassesByCapacity(classUuids);

            log.info("课程 {} 拆分后得到 {} 个分组", courseUuid, splitClassGroups.size());

            // 3. 为每个分组创建教学班
            for (int i = 0; i < splitClassGroups.size(); i++) {
                List<String> group = splitClassGroups.get(i);
                String groupSuffix = splitClassGroups.size() > 1 ? "-" + (i + 1) + "组" : "";

                log.info("处理分组 {}: 行政班UUID列表 = {}", i, group);

                // 为分组创建独立的教学班
                String teachingClassUuid = findOrBuildTeachingClass(
                        courseUuid, teacherUuid, semesterUuid, group, groupSuffix);
                teachingClassUuids.add(teachingClassUuid);

                log.info("课程: {}, 教师: {}, 行政班: {} -> 教学班: {}",
                        courseUuid, teacherUuid, group, teachingClassUuid);
            }
        }

        log.info("最终创建/复用的教学班UUID数量: {}", teachingClassUuids.size());

        return teachingClassUuids;
    }

    /**
     * 按容量拆分行政班
     * 贪心算法：尽量让每个教学班接近容量上限
     *
     * @param classUuids 行政班UUID列表
     * @return 拆分后的行政班分组列表
     */
    private List<List<String>> splitClassesByCapacity(List<String> classUuids) {
        List<List<String>> groups = new ArrayList<>();

        // 计算每个行政班的学生人数
        Map<String, Integer> classStudentCount = new HashMap<>();
        for (String classUuid : classUuids) {
            List<StudentDO> students = studentDAO.list(
                    new QueryWrapper<StudentDO>().eq("class_uuid", classUuid)
            );
            int count = students.size();
            classStudentCount.put(classUuid, count);
            log.debug("行政班 {} 学生人数: {}", classUuid, count);
        }

        log.info("行政班数量: {}, 学生人数分布: {}", classUuids.size(), classStudentCount.toString());

        // 按班级人数降序排序，优先处理大班
        List<String> sortedClassUuids = new ArrayList<>(classUuids);
        sortedClassUuids.sort((a, b) -> Integer.compare(
                classStudentCount.getOrDefault(b, 0),
                classStudentCount.getOrDefault(a, 0)
        ));

        // 贪心分配：每个班尽量装满到容量上限
        List<String> currentGroup = new ArrayList<>();
        int currentGroupCapacity = 0;

        for (String classUuid : sortedClassUuids) {
            int studentCount = classStudentCount.getOrDefault(classUuid, 0);

            // 如果单个行政班就超过容量，直接作为一个组
            if (studentCount > MAX_TEACHING_CLASS_CAPACITY) {
                // 先把当前的组保存
                if (!currentGroup.isEmpty()) {
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                    currentGroupCapacity = 0;
                }
                // 这个班单独成一组
                groups.add(Collections.singletonList(classUuid));
                continue;
            }

            // 尝试加入当前组
            if (currentGroupCapacity + studentCount <= MAX_TEACHING_CLASS_CAPACITY) {
                currentGroup.add(classUuid);
                currentGroupCapacity += studentCount;
            } else {
                // 当前组已满，保存并创建新组
                if (!currentGroup.isEmpty()) {
                    groups.add(new ArrayList<>(currentGroup));
                }
                currentGroup = new ArrayList<>();
                currentGroup.add(classUuid);
                currentGroupCapacity = studentCount;
            }
        }

        // 保存最后一个组
        if (!currentGroup.isEmpty()) {
            groups.add(new ArrayList<>(currentGroup));
        }

        return groups;
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
     * @param groupSuffix 分组后缀（如"-1组"，为空则不添加）
     * @return 教学班UUID
     */
    private String findOrBuildTeachingClass(String courseUuid, String teacherUuid,
                                              String semesterUuid, List<String> classUuids, String groupSuffix) {
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
        return createNewTeachingClass(courseUuid, teacherUuid, semesterUuid, classUuids, groupSuffix);
    }

    /**
     * 创建新的教学班
     *
     * @param courseUuid 课程UUID
     * @param teacherUuid 教师UUID
     * @param semesterUuid 学期UUID
     * @param classUuids 行政班UUID列表
     * @param groupSuffix 分组后缀（如"-1组"，为空则不添加）
     * @return 教学班UUID
     */
    private String createNewTeachingClass(String courseUuid, String teacherUuid,
                                          String semesterUuid, List<String> classUuids, String groupSuffix) {
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

        // 4. 构建教学班名称：课程名-教师名-班级数+后缀
        String classCountInfo = classUuids.size() + "班" + groupSuffix;
        String teachingClassName = course.getCourseName() + "-" + teacher.getTeacherName() + "-" + classCountInfo;

        // 限制名称长度不超过64字符
        if (teachingClassName.length() > 64) {
            // 截断课程名，保留教师名和班级信息
            int maxCourseNameLen = 64 - teacher.getTeacherName().length() - classCountInfo.length() - 2;
            if (maxCourseNameLen > 0) {
                String shortCourseName = course.getCourseName().substring(0, Math.min(course.getCourseName().length(), maxCourseNameLen));
                teachingClassName = shortCourseName + "-" + teacher.getTeacherName() + "-" + classCountInfo;
            } else {
                teachingClassName = teachingClassName.substring(0, 64);
            }
        }

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
