package io.github.flashlack1314.smartschedulecorev2.algorithm.core;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.Conflict;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ConflictReport;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleContext;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.Chromosome;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 冲突检测器：检测排课方案中的所有冲突
 *
 * @author flash
 */
@Slf4j
@Component
public class ConflictDetector {

    /**
     * 检测染色体中的所有冲突
     *
     * @param chromosome 染色体
     * @param context    排课上下文
     * @return 冲突报告
     */
    public ConflictReport detectConflicts(Chromosome chromosome, ScheduleContext context) {
        ConflictReport report = new ConflictReport();

        // 1. 检测与已有排课的冲突
        detectConflictsWithExistingSchedules(chromosome, context, report);

        // 2. 检测新排课内部的冲突
        detectInternalConflicts(chromosome, context, report);

        return report;
    }

    /**
     * 检测与已有正式排课（status=1）的冲突
     */
    private void detectConflictsWithExistingSchedules(Chromosome chromosome, ScheduleContext context, ConflictReport report) {
        if (context.getExistingSchedules() == null || context.getExistingSchedules().isEmpty()) {
            return;
        }

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : chromosome.getGenes().entrySet()) {
            TimeSlot newSlot = entry.getKey();

            for (CourseAppointment newAppt : entry.getValue()) {
                for (ScheduleContext.ExistingSchedule existing : context.getExistingSchedules()) {
                    TimeSlot existingSlot = existing.getTimeSlot();

                    if (newSlot.isOverlap(existingSlot)) {
                        // 检查教师冲突
                        if (newAppt.getTeacherUuid().equals(existing.getTeacherUuid())) {
                            report.addHardConflict(createConflict(
                                    Conflict.ConflictType.EXISTING_SCHEDULE_CONFLICT,
                                    "教师[" + newAppt.getTeacherName() + "]在已有排课中该时间段有课",
                                    newAppt, Conflict.ConflictSeverity.HARD
                            ));
                        }

                        // 检查教室冲突
                        if (newAppt.getClassroomUuid().equals(existing.getClassroomUuid())) {
                            report.addHardConflict(createConflict(
                                    Conflict.ConflictType.EXISTING_SCHEDULE_CONFLICT,
                                    "教室[" + newAppt.getClassroomName() + "]在已有排课中该时间段已被占用",
                                    newAppt, Conflict.ConflictSeverity.HARD
                            ));
                        }

                        // 检查班级冲突
                        if (newAppt.getClassUuids() != null && existing.getClassUuids() != null) {
                            Set<String> newClasses = new HashSet<>(newAppt.getClassUuids());
                            Set<String> existingClasses = new HashSet<>(existing.getClassUuids());
                            newClasses.retainAll(existingClasses);

                            if (!newClasses.isEmpty()) {
                                report.addHardConflict(createConflict(
                                        Conflict.ConflictType.EXISTING_SCHEDULE_CONFLICT,
                                        "班级在已有排课中该时间段有课",
                                        newAppt, Conflict.ConflictSeverity.HARD
                                ));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 检测新排课内部的冲突
     */
    private void detectInternalConflicts(Chromosome chromosome, ScheduleContext context, ConflictReport report) {
        List<CourseAppointment> allAppointments = chromosome.getGenes().values().stream()
                .flatMap(List::stream)
                .toList();

        // 两两比较检测冲突
        for (int i = 0; i < allAppointments.size(); i++) {
            for (int j = i + 1; j < allAppointments.size(); j++) {
                CourseAppointment appt1 = allAppointments.get(i);
                CourseAppointment appt2 = allAppointments.get(j);

                // 跳过同一教学班的不同次排课
                if (appt1.getTeachingClassUuid().equals(appt2.getTeachingClassUuid())) {
                    continue;
                }

                TimeSlot slot1 = appt1.getTimeSlot();
                TimeSlot slot2 = appt2.getTimeSlot();

                if (slot1.isOverlap(slot2)) {
                    // 检测教师冲突
                    if (appt1.getTeacherUuid().equals(appt2.getTeacherUuid())) {
                        report.addHardConflict(createConflict(
                                Conflict.ConflictType.TEACHER_TIME_CONFLICT,
                                "教师[" + appt1.getTeacherName() + "]同一时间有多门课程",
                                appt1, Conflict.ConflictSeverity.HARD
                        ));
                    }

                    // 检测教室冲突
                    if (appt1.getClassroomUuid().equals(appt2.getClassroomUuid())) {
                        report.addHardConflict(createConflict(
                                Conflict.ConflictType.CLASSROOM_TIME_CONFLICT,
                                "教室[" + appt1.getClassroomName() + "]同一时间被多个课程占用",
                                appt1, Conflict.ConflictSeverity.HARD
                        ));
                    }

                    // 检测班级冲突
                    if (appt1.getClassUuids() != null && appt2.getClassUuids() != null) {
                        Set<String> classes1 = new HashSet<>(appt1.getClassUuids());
                        Set<String> classes2 = new HashSet<>(appt2.getClassUuids());
                        classes1.retainAll(classes2);

                        if (!classes1.isEmpty()) {
                            report.addHardConflict(createConflict(
                                    Conflict.ConflictType.CLASS_TIME_CONFLICT,
                                    "班级同一时间有多门课程",
                                    appt1, Conflict.ConflictSeverity.HARD
                            ));
                        }
                    }
                }
            }
        }

        // 检测容量约束
        detectCapacityConflicts(allAppointments, report);

        // 检测教室类型匹配
        detectClassroomTypeConflicts(allAppointments, context, report);

        // 检测教师资格约束
        detectQualificationConflicts(allAppointments, context, report);

        // 检测合班上课约束
        detectCombinedClassConflicts(chromosome, context, report);
    }

    /**
     * 检测教室容量约束
     */
    private void detectCapacityConflicts(List<CourseAppointment> appointments, ConflictReport report) {
        for (CourseAppointment appt : appointments) {
            if (appt.getClassroomCapacity() != null && appt.getTotalStudents() != null) {
                if (appt.getClassroomCapacity() < appt.getTotalStudents()) {
                    report.addHardConflict(createConflict(
                            Conflict.ConflictType.CAPACITY_INSUFFICIENT,
                            "教室[" + appt.getClassroomName() + "]容量不足，需要" + appt.getTotalStudents() + "个座位，实际只有" + appt.getClassroomCapacity() + "个",
                            appt, Conflict.ConflictSeverity.HARD
                    ));
                }
            }
        }
    }

    /**
     * 检测教室类型匹配约束
     *
     * @param context 排课上下文，包含课程类型-教室类型映射
     */
    private void detectClassroomTypeConflicts(List<CourseAppointment> appointments, ScheduleContext context, ConflictReport report) {
        // 此方法需要额外的课程类型-教室类型映射数据
        // 当前简化实现，后续可以从context获取映射关系进行检测
    }

    /**
     * 检测教师资格约束
     *
     * @param context 排课上下文，包含课程-教师资质映射
     */
    private void detectQualificationConflicts(List<CourseAppointment> appointments, ScheduleContext context, ConflictReport report) {
        // 此方法需要额外的课程-教师资质映射数据
        // 当前简化实现，后续可以从context获取资质关系进行检测
    }

    /**
     * 检测单个课程安排是否与染色体中已有安排冲突
     *
     * @param chromosome 染色体
     * @param appointment 新的课程安排
     * @param context 排课上下文
     * @return 是否存在冲突
     */
    public boolean hasConflict(Chromosome chromosome, CourseAppointment appointment, ScheduleContext context) {
        TimeSlot newSlot = appointment.getTimeSlot();

        // 检查与已有排课的冲突
        if (context.getExistingSchedules() != null) {
            for (ScheduleContext.ExistingSchedule existing : context.getExistingSchedules()) {
                if (newSlot.isOverlap(existing.getTimeSlot())) {
                    // 教师冲突
                    if (appointment.getTeacherUuid().equals(existing.getTeacherUuid())) {
                        return true;
                    }
                    // 教室冲突
                    if (appointment.getClassroomUuid().equals(existing.getClassroomUuid())) {
                        return true;
                    }
                    // 班级冲突
                    if (appointment.getClassUuids() != null && existing.getClassUuids() != null) {
                        Set<String> newClasses = new HashSet<>(appointment.getClassUuids());
                        Set<String> existingClasses = new HashSet<>(existing.getClassUuids());
                        newClasses.retainAll(existingClasses);
                        if (!newClasses.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }

        // 检查与染色体内部已有安排的冲突
        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : chromosome.getGenes().entrySet()) {
            TimeSlot existingSlot = entry.getKey();

            if (newSlot.isOverlap(existingSlot)) {
                for (CourseAppointment existingAppt : entry.getValue()) {
                    // 同一教学班跳过
                    if (appointment.getTeachingClassUuid().equals(existingAppt.getTeachingClassUuid())) {
                        continue;
                    }

                    // 教师冲突
                    if (appointment.getTeacherUuid().equals(existingAppt.getTeacherUuid())) {
                        return true;
                    }
                    // 教室冲突
                    if (appointment.getClassroomUuid().equals(existingAppt.getClassroomUuid())) {
                        return true;
                    }
                    // 班级冲突
                    if (appointment.getClassUuids() != null && existingAppt.getClassUuids() != null) {
                        Set<String> newClasses = new HashSet<>(appointment.getClassUuids());
                        Set<String> existingClasses = new HashSet<>(existingAppt.getClassUuids());
                        newClasses.retainAll(existingClasses);
                        if (!newClasses.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 创建冲突对象
     */
    private Conflict createConflict(Conflict.ConflictType type, String description,
                                    CourseAppointment appt, Conflict.ConflictSeverity severity) {
        Conflict conflict = new Conflict();
        conflict.setType(type);
        conflict.setDescription(description);
        conflict.setTeachingClassUuid(appt.getTeachingClassUuid());
        conflict.setTeacherUuid(appt.getTeacherUuid());
        conflict.setClassroomUuid(appt.getClassroomUuid());
        conflict.setSeverity(severity);
        return conflict;
    }

    /**
     * 检测合班上课时间冲突
     *
     * 约束规则：同一课程对应的多个行政班必须在同一时间上课
     */
    private void detectCombinedClassConflicts(Chromosome chromosome, ScheduleContext context, ConflictReport report) {
        if (context.getCourseClassMapping() == null || context.getCourseClassMapping().isEmpty()) {
            return;
        }

        // 按课程UUID分组所有课程安排
        Map<String, List<CourseAppointment>> appointmentsByCourse = new HashMap<>();

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : chromosome.getGenes().entrySet()) {
            for (CourseAppointment appt : entry.getValue()) {
                String courseUuid = appt.getCourseUuid();
                appointmentsByCourse.computeIfAbsent(courseUuid, k -> new ArrayList<>()).add(appt);
            }
        }

        // 检查每个课程的教学班时间是否一致
        for (Map.Entry<String, List<String>> mappingEntry : context.getCourseClassMapping().entrySet()) {
            String courseUuid = mappingEntry.getKey();
            List<String> expectedClassUuids = new ArrayList<>(mappingEntry.getValue());
            Collections.sort(expectedClassUuids);

            List<CourseAppointment> appointments = appointmentsByCourse.get(courseUuid);
            if (appointments == null || appointments.isEmpty()) {
                continue;
            }

            // 检查同一课程是否有多个教学班且时间不同
            TimeSlot referenceTimeSlot = null;
            String referenceTeachingClass = null;

            for (CourseAppointment appt : appointments) {
                List<String> actualClassUuids = new ArrayList<>(appt.getClassUuids());
                Collections.sort(actualClassUuids);

                // 如果行政班列表匹配，记录参考时间
                if (actualClassUuids.equals(expectedClassUuids)) {
                    if (referenceTimeSlot == null) {
                        referenceTimeSlot = appt.getTimeSlot();
                        referenceTeachingClass = appt.getTeachingClassUuid();
                    } else {
                        // 检查时间是否一致
                        if (!referenceTimeSlot.equals(appt.getTimeSlot())) {
                            report.addHardConflict(createConflict(
                                    Conflict.ConflictType.COMBINED_CLASS_TIME_CONFLICT,
                                    "课程[" + appt.getCourseName() + "]的合班上课时间不一致，" +
                                    "教学班[" + referenceTeachingClass + "]和[" + appt.getTeachingClassUuid() + "]应在同一时间上课",
                                    appt, Conflict.ConflictSeverity.HARD
                            ));
                        }
                    }
                }
            }
        }
    }
}
