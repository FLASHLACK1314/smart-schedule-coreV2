package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排课冲突初始化器
 * 负责在FULL模式下初始化排课冲突测试数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleConflictInitializer {

    private final ScheduleConflictDAO scheduleConflictDAO;
    private final ScheduleDAO scheduleDAO;
    private final ClassroomDAO classroomDAO;
    private final ClassDAO classDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final ObjectMapper objectMapper;

    /**
     * 初始化排课冲突数据
     * 检测现有排课中的冲突并记录
     *
     * @param schedules 排课记录列表
     * @param semesters 学期列表
     */
    public void initializeScheduleConflicts(List<ScheduleDO> schedules, List<SemesterDO> semesters) {
        log.info("正在初始化排课冲突记录...");

        if (schedules == null || schedules.isEmpty()) {
            // 如果传入的为空，从数据库重新获取
            schedules = scheduleDAO.list();
        }

        if (schedules.isEmpty()) {
            log.warn("没有排课记录，跳过冲突初始化");
            return;
        }

        List<ScheduleConflictDO> conflicts = new ArrayList<>();

        // 构建辅助映射
        Map<String, ClassroomDO> classroomMap = classroomDAO.list().stream()
                .collect(Collectors.toMap(ClassroomDO::getClassroomUuid, c -> c));
        Map<String, CourseDO> courseMap = courseDAO.list().stream()
                .collect(Collectors.toMap(CourseDO::getCourseUuid, c -> c));
        Map<String, TeacherDO> teacherMap = teacherDAO.list().stream()
                .collect(Collectors.toMap(TeacherDO::getTeacherUuid, t -> t));

        // 获取教学班-行政班级关联
        List<TeachingClassClassDO> teachingClassClasses = teachingClassClassDAO.list();
        Map<String, List<String>> teachingClassToClassMap = new HashMap<>();
        for (TeachingClassClassDO tcc : teachingClassClasses) {
            teachingClassToClassMap.computeIfAbsent(tcc.getTeachingClassUuid(), k -> new ArrayList<>())
                    .add(tcc.getClassUuid());
        }

        // 统计每个班级的学生数量
        Map<String, Long> classStudentCountMap = new HashMap<>();
        for (ClassDO classDO : classDAO.list()) {
            classStudentCountMap.put(classDO.getClassUuid(), studentDAO.countByClassUuid(classDO.getClassUuid()));
        }

        // 1. 检测教师时间冲突
        int teacherConflicts = detectTeacherTimeConflicts(schedules, courseMap, teacherMap, conflicts);
        log.info("检测到教师时间冲突: {}条", teacherConflicts);

        // 2. 检测教室时间冲突
        int classroomConflicts = detectClassroomTimeConflicts(schedules, courseMap, classroomMap, conflicts);
        log.info("检测到教室时间冲突: {}条", classroomConflicts);

        // 3. 检测班级时间冲突
        int classConflicts = detectClassTimeConflicts(schedules, teachingClassToClassMap, courseMap, conflicts);
        log.info("检测到班级时间冲突: {}条", classConflicts);

        // 4. 检测教室容量不足
        int capacityConflicts = detectCapacityConflicts(schedules, classroomMap, teachingClassToClassMap,
                classStudentCountMap, courseMap, conflicts);
        log.info("检测到教室容量不足冲突: {}条", capacityConflicts);

        // 5. 生成教师时间偏好未满足的软冲突
        int preferenceConflicts = detectTeacherPreferenceConflicts(schedules, teacherMap, courseMap, conflicts);
        log.info("检测到教师时间偏好未满足: {}条", preferenceConflicts);

        // 保存冲突记录
        if (!conflicts.isEmpty()) {
            scheduleConflictDAO.saveBatch(conflicts);
            log.info("排课冲突记录初始化完成，共 {} 条记录", conflicts.size());
        } else {
            log.info("未检测到排课冲突");
        }
    }

    /**
     * 检测教师时间冲突
     * 同一教师在同一时间有多门课程
     */
    private int detectTeacherTimeConflicts(
            List<ScheduleDO> schedules,
            Map<String, CourseDO> courseMap,
            Map<String, TeacherDO> teacherMap,
            List<ScheduleConflictDO> conflicts) {

        int count = 0;
        // 按教师分组
        Map<String, List<ScheduleDO>> byTeacher = schedules.stream()
                .filter(s -> s.getTeacherUuid() != null)
                .collect(Collectors.groupingBy(ScheduleDO::getTeacherUuid));

        for (Map.Entry<String, List<ScheduleDO>> entry : byTeacher.entrySet()) {
            String teacherUuid = entry.getKey();
            List<ScheduleDO> teacherSchedules = entry.getValue();

            if (teacherSchedules.size() < 2) continue;

            // 两两比较
            for (int i = 0; i < teacherSchedules.size(); i++) {
                for (int j = i + 1; j < teacherSchedules.size(); j++) {
                    ScheduleDO a = teacherSchedules.get(i);
                    ScheduleDO b = teacherSchedules.get(j);

                    if (isTimeOverlap(a, b)) {
                        TeacherDO teacher = teacherMap.get(teacherUuid);
                        CourseDO courseA = courseMap.get(a.getCourseUuid());
                        CourseDO courseB = courseMap.get(b.getCourseUuid());

                        String teacherName = teacher != null ? teacher.getTeacherName() : "未知教师";
                        String courseNameA = courseA != null ? courseA.getCourseName() : "未知课程";
                        String courseNameB = courseB != null ? courseB.getCourseName() : "未知课程";

                        conflicts.add(createConflict(
                                a, b,
                                "TEACHER_TIME_CONFLICT",
                                1,
                                String.format("教师[%s]在周%s第%d-%d节同时有课程[%s]和[%s]",
                                        teacherName,
                                        getDayName(a.getDayOfWeek()),
                                        a.getSectionStart(),
                                        a.getSectionEnd(),
                                        courseNameA,
                                        courseNameB)
                        ));
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 检测教室时间冲突
     * 同一教室在同一时间被安排多门课程
     */
    private int detectClassroomTimeConflicts(
            List<ScheduleDO> schedules,
            Map<String, CourseDO> courseMap,
            Map<String, ClassroomDO> classroomMap,
            List<ScheduleConflictDO> conflicts) {

        int count = 0;
        // 按教室分组
        Map<String, List<ScheduleDO>> byClassroom = schedules.stream()
                .filter(s -> s.getClassroomUuid() != null)
                .collect(Collectors.groupingBy(ScheduleDO::getClassroomUuid));

        for (Map.Entry<String, List<ScheduleDO>> entry : byClassroom.entrySet()) {
            String classroomUuid = entry.getKey();
            List<ScheduleDO> classroomSchedules = entry.getValue();

            if (classroomSchedules.size() < 2) continue;

            // 两两比较
            for (int i = 0; i < classroomSchedules.size(); i++) {
                for (int j = i + 1; j < classroomSchedules.size(); j++) {
                    ScheduleDO a = classroomSchedules.get(i);
                    ScheduleDO b = classroomSchedules.get(j);

                    if (isTimeOverlap(a, b)) {
                        ClassroomDO classroom = classroomMap.get(classroomUuid);
                        CourseDO courseA = courseMap.get(a.getCourseUuid());
                        CourseDO courseB = courseMap.get(b.getCourseUuid());

                        String classroomName = classroom != null ? classroom.getClassroomName() : "未知教室";
                        String courseNameA = courseA != null ? courseA.getCourseName() : "未知课程";
                        String courseNameB = courseB != null ? courseB.getCourseName() : "未知课程";

                        conflicts.add(createConflict(
                                a, b,
                                "CLASSROOM_TIME_CONFLICT",
                                1,
                                String.format("教室[%s]在周%s第%d-%d节被重复安排了课程[%s]和[%s]",
                                        classroomName,
                                        getDayName(a.getDayOfWeek()),
                                        a.getSectionStart(),
                                        a.getSectionEnd(),
                                        courseNameA,
                                        courseNameB)
                        ));
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 检测班级时间冲突
     * 同一班级在同一时间有多门课程
     */
    private int detectClassTimeConflicts(
            List<ScheduleDO> schedules,
            Map<String, List<String>> teachingClassToClassMap,
            Map<String, CourseDO> courseMap,
            List<ScheduleConflictDO> conflicts) {

        int count = 0;
        // 建立班级到排课的映射
        Map<String, List<ScheduleDO>> classToSchedules = new HashMap<>();

        for (ScheduleDO schedule : schedules) {
            List<String> classUuids = teachingClassToClassMap.get(schedule.getTeachingClassUuid());
            if (classUuids != null) {
                for (String classUuid : classUuids) {
                    classToSchedules.computeIfAbsent(classUuid, k -> new ArrayList<>()).add(schedule);
                }
            }
        }

        // 检测每个班级的排课冲突
        for (Map.Entry<String, List<ScheduleDO>> entry : classToSchedules.entrySet()) {
            List<ScheduleDO> classSchedules = entry.getValue();

            if (classSchedules.size() < 2) continue;

            // 两两比较
            for (int i = 0; i < classSchedules.size(); i++) {
                for (int j = i + 1; j < classSchedules.size(); j++) {
                    ScheduleDO a = classSchedules.get(i);
                    ScheduleDO b = classSchedules.get(j);

                    // 排除同一教学班的排课（它们是同一门课的不同时间段）
                    if (a.getTeachingClassUuid().equals(b.getTeachingClassUuid())) {
                        continue;
                    }

                    if (isTimeOverlap(a, b)) {
                        CourseDO courseA = courseMap.get(a.getCourseUuid());
                        CourseDO courseB = courseMap.get(b.getCourseUuid());

                        String courseNameA = courseA != null ? courseA.getCourseName() : "未知课程";
                        String courseNameB = courseB != null ? courseB.getCourseName() : "未知课程";

                        conflicts.add(createConflict(
                                a, b,
                                "CLASS_TIME_CONFLICT",
                                1,
                                String.format("班级在周%s第%d-%d节同时有课程[%s]和[%s]",
                                        getDayName(a.getDayOfWeek()),
                                        a.getSectionStart(),
                                        a.getSectionEnd(),
                                        courseNameA,
                                        courseNameB)
                        ));
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 检测教室容量不足冲突
     * 教室容量小于班级总学生数
     */
    private int detectCapacityConflicts(
            List<ScheduleDO> schedules,
            Map<String, ClassroomDO> classroomMap,
            Map<String, List<String>> teachingClassToClassMap,
            Map<String, Long> classStudentCountMap,
            Map<String, CourseDO> courseMap,
            List<ScheduleConflictDO> conflicts) {

        int count = 0;

        for (ScheduleDO schedule : schedules) {
            ClassroomDO classroom = classroomMap.get(schedule.getClassroomUuid());
            if (classroom == null || classroom.getClassroomCapacity() == null) {
                continue;
            }

            // 计算该教学班对应的所有班级的学生总数
            List<String> classUuids = teachingClassToClassMap.get(schedule.getTeachingClassUuid());
            if (classUuids == null || classUuids.isEmpty()) {
                continue;
            }

            int totalStudents = 0;
            for (String classUuid : classUuids) {
                Long studentCount = classStudentCountMap.get(classUuid);
                if (studentCount != null) {
                    totalStudents += studentCount.intValue();
                }
            }

            // 如果学生总数超过教室容量，记录冲突
            if (totalStudents > classroom.getClassroomCapacity()) {
                CourseDO course = courseMap.get(schedule.getCourseUuid());
                String courseName = course != null ? course.getCourseName() : "未知课程";

                conflicts.add(createCapacityConflict(
                        schedule,
                        "CAPACITY_INSUFFICIENT",
                        1,
                        String.format("教室[%s]容量%d人，无法容纳课程[%s]的总学生数%d人",
                                classroom.getClassroomName(),
                                classroom.getClassroomCapacity(),
                                courseName,
                                totalStudents)
                ));
                count++;
            }
        }
        return count;
    }

    /**
     * 检测教师时间偏好未满足的软冲突
     */
    private int detectTeacherPreferenceConflicts(
            List<ScheduleDO> schedules,
            Map<String, TeacherDO> teacherMap,
            Map<String, CourseDO> courseMap,
            List<ScheduleConflictDO> conflicts) {

        int count = 0;

        for (ScheduleDO schedule : schedules) {
            TeacherDO teacher = teacherMap.get(schedule.getTeacherUuid());
            if (teacher == null || teacher.getLikeTime() == null || teacher.getLikeTime().isEmpty()) {
                continue;
            }

            // 解析教师时间偏好
            String likeTime = teacher.getLikeTime();
            // 假设偏好格式如 "1-3,2-5" 表示周一第3节和周二第5节
            // 或 "1,2,3" 表示周一、周二、周三

            if (!isPreferredTime(schedule, likeTime)) {
                CourseDO course = courseMap.get(schedule.getCourseUuid());
                String courseName = course != null ? course.getCourseName() : "未知课程";

                conflicts.add(createPreferenceConflict(
                        schedule,
                        "TEACHER_PREFERENCE_NOT_MET",
                        0, // 软冲突
                        String.format("教师[%s]的时间偏好[%s]未满足，课程[%s]被安排在周%s第%d-%d节",
                                teacher.getTeacherName(),
                                likeTime,
                                courseName,
                                getDayName(schedule.getDayOfWeek()),
                                schedule.getSectionStart(),
                                schedule.getSectionEnd())
                ));
                count++;
            }
        }
        return count;
    }

    /**
     * 判断两个排课记录是否有时间重叠
     */
    private boolean isTimeOverlap(ScheduleDO a, ScheduleDO b) {
        // 1. 同一学期
        if (!Objects.equals(a.getSemesterUuid(), b.getSemesterUuid())) {
            return false;
        }

        // 2. 同一星期几
        if (!Objects.equals(a.getDayOfWeek(), b.getDayOfWeek())) {
            return false;
        }

        // 3. 节次有重叠
        boolean sectionOverlap = !(a.getSectionEnd() < b.getSectionStart() ||
                a.getSectionStart() > b.getSectionEnd());
        if (!sectionOverlap) {
            return false;
        }

        // 4. 周次有重叠
        return hasWeeksOverlap(a.getWeeksJson(), b.getWeeksJson());
    }

    /**
     * 判断两个周次JSON数组是否有重叠
     */
    private boolean hasWeeksOverlap(String weeksJsonA, String weeksJsonB) {
        if (weeksJsonA == null || weeksJsonB == null) {
            return true; // 如果没有周次信息，假设有重叠
        }

        try {
            JsonNode weeksA = objectMapper.readTree(weeksJsonA);
            JsonNode weeksB = objectMapper.readTree(weeksJsonB);

            Set<Integer> setA = new HashSet<>();
            for (JsonNode node : weeksA) {
                setA.add(node.asInt());
            }

            for (JsonNode node : weeksB) {
                if (setA.contains(node.asInt())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("解析周次JSON失败: {}", e.getMessage());
            return true; // 解析失败时假设有重叠
        }
    }

    /**
     * 判断排课时间是否符合教师偏好
     * 偏好格式支持: "1,2,3" (周一、二、三) 或 "1-3" (周一第3节)
     */
    private boolean isPreferredTime(ScheduleDO schedule, String likeTime) {
        if (likeTime == null || likeTime.isEmpty()) {
            return true; // 没有偏好则认为都满足
        }

        int dayOfWeek = schedule.getDayOfWeek();

        // 简单实现：检查偏好中是否包含当前的星期
        String[] preferences = likeTime.split(",");
        for (String pref : preferences) {
            pref = pref.trim();
            if (pref.contains("-")) {
                // 格式 "1-3" 表示周一第3节
                String[] parts = pref.split("-");
                if (parts.length >= 1) {
                    try {
                        int prefDay = Integer.parseInt(parts[0].trim());
                        if (prefDay == dayOfWeek) {
                            return true;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                // 格式 "1" 表示周一
                try {
                    int prefDay = Integer.parseInt(pref);
                    if (prefDay == dayOfWeek) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    /**
     * 创建冲突记录（双排课记录冲突）
     */
    private ScheduleConflictDO createConflict(
            ScheduleDO a, ScheduleDO b,
            String conflictType, int severity, String description) {
        ScheduleConflictDO conflict = new ScheduleConflictDO();
        conflict.setConflictUuid(UuidUtil.generateUuidNoDash())
                .setSemesterUuid(a.getSemesterUuid())
                .setScheduleUuidA(a.getScheduleUuid())
                .setScheduleUuidB(b.getScheduleUuid())
                .setConflictType(conflictType)
                .setSeverity(severity)
                .setDescription(description);
        return conflict;
    }

    /**
     * 创建冲突记录（单排课记录冲突，如容量不足）
     */
    private ScheduleConflictDO createCapacityConflict(
            ScheduleDO schedule,
            String conflictType, int severity, String description) {
        ScheduleConflictDO conflict = new ScheduleConflictDO();
        conflict.setConflictUuid(UuidUtil.generateUuidNoDash())
                .setSemesterUuid(schedule.getSemesterUuid())
                .setScheduleUuidA(schedule.getScheduleUuid())
                .setScheduleUuidB(null) // 单条记录冲突
                .setConflictType(conflictType)
                .setSeverity(severity)
                .setDescription(description);
        return conflict;
    }

    /**
     * 创建软冲突记录
     */
    private ScheduleConflictDO createPreferenceConflict(
            ScheduleDO schedule,
            String conflictType, int severity, String description) {
        ScheduleConflictDO conflict = new ScheduleConflictDO();
        conflict.setConflictUuid(UuidUtil.generateUuidNoDash())
                .setSemesterUuid(schedule.getSemesterUuid())
                .setScheduleUuidA(schedule.getScheduleUuid())
                .setScheduleUuidB(null)
                .setConflictType(conflictType)
                .setSeverity(severity)
                .setDescription(description);
        return conflict;
    }

    /**
     * 获取星期名称
     */
    private String getDayName(Integer dayOfWeek) {
        if (dayOfWeek == null) return "未知";
        String[] days = {"", "一", "二", "三", "四", "五", "六", "日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "未知";
    }
}
