package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 自动排课结果
 *
 * @author flash
 */
@Data
public class AutoScheduleResult {
    /**
     * 学期UUID
     */
    private String semesterUuid;

    /**
     * 排课方案：时间槽 -> 该时间段的所有课程安排
     */
    private Map<TimeSlot, List<CourseAppointment>> scheduleMap;

    /**
     * 总体适应度分数
     */
    private Double fitness;

    /**
     * 硬约束冲突数量
     */
    private int hardConflicts;

    /**
     * 软约束冲突数量
     */
    private int softConflicts;

    /**
     * 未完成排课的教学班列表
     */
    private List<String> unscheduledTeachingClasses;

    /**
     * 冲突报告
     */
    private ConflictReport conflictReport;

    /**
     * 排课统计信息
     */
    private ScheduleStatistics statistics;

    /**
     * 排课是否成功（无硬冲突且完成所有排课）
     */
    public boolean isSuccess() {
        return hardConflicts == 0 &&
                (unscheduledTeachingClasses == null || unscheduledTeachingClasses.isEmpty());
    }

    /**
     * 排课统计信息
     */
    @Data
    public static class ScheduleStatistics {
        private int totalTeachingClasses;
        private int scheduledTeachingClasses;
        private int totalSessions;
        private int totalHours;
        private double averageFitness;
    }
}
