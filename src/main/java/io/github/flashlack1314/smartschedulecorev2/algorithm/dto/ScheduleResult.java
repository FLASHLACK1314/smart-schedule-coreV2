package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 排课结果
 *
 * @author flash
 */
@Data
public class ScheduleResult {
    /**
     * 学期UUID
     */
    private String semesterUuid;

    /**
     * 排课方案：教学班UUID -> 课程安排列表
     */
    private Map<String, List<CourseAppointment>> scheduleMap;

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
     * 排课统计信息
     */
    @Data
    public static class ScheduleStatistics {
        private int totalTeachingClasses;
        private int scheduledTeachingClasses;
        private int totalSessions;
        private int totalHours;
        private int averageFitness;
    }
}
