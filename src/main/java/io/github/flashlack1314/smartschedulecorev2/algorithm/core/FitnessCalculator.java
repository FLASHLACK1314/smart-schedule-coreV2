package io.github.flashlack1314.smartschedulecorev2.algorithm.core;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.Conflict;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ConflictReport;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleContext;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.Chromosome;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 适应度计算器：计算排课方案的适应度
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FitnessCalculator {

    private final ConflictDetector conflictDetector;

    // 硬约束权重配置
    private static final int TEACHER_CONFLICT_PENALTY = 1_000_000;
    private static final int CLASSROOM_CONFLICT_PENALTY = 1_000_000;
    private static final int CLASS_CONFLICT_PENALTY = 1_000_000;
    private static final int CAPACITY_PENALTY = 500_000;
    private static final int TYPE_MISMATCH_PENALTY = 500_000;
    private static final int QUALIFICATION_PENALTY = 1_000_000;
    private static final int EXISTING_CONFLICT_PENALTY = 1_000_000;

    // 软约束权重配置
    private static final int TEACHER_PREFERENCE_REWARD = 1000;
    private static final int WORKLOAD_BALANCE_REWARD = 500;
    private static final int COURSE_DISTRIBUTION_REWARD = 300;

    // 未完成排课惩罚
    private static final int INCOMPLETE_SCHEDULE_PENALTY = 2_000_000;

    /**
     * 计算染色体适应度
     *
     * @param chromosome 染色体
     * @param context    排课上下文
     */
    public void calculateFitness(Chromosome chromosome, ScheduleContext context) {
        ConflictReport report = conflictDetector.detectConflicts(chromosome, context);

        // 硬约束惩罚（负分）
        double hardPenalty = 0;
        for (Conflict conflict : report.getHardConflicts()) {
            hardPenalty += getPenalty(conflict.getType());
        }

        // 添加详细日志（使用 INFO 级别以便调试）
        if (!report.getHardConflicts().isEmpty()) {
            Map<Conflict.ConflictType, Long> conflictCounts = report.getHardConflicts().stream()
                    .collect(Collectors.groupingBy(Conflict::getType, Collectors.counting()));
            log.info("检测到 {} 个硬约束冲突，按类型统计: {}", report.getHardConflicts().size(), conflictCounts);

            // 输出每个冲突的详细信息（最多显示前5个）
            int shown = 0;
            for (Conflict conflict : report.getHardConflicts()) {
                if (shown++ >= 5) {
                    log.info("... 还有 {} 个冲突未显示", report.getHardConflicts().size() - 5);
                    break;
                }
                log.info("  冲突详情: {} - {}", conflict.getType(), conflict.getDescription());
            }
        }

        // 未完成排课惩罚
        hardPenalty += chromosome.getUnscheduledTeachingClasses().size() * INCOMPLETE_SCHEDULE_PENALTY;

        // 软约束奖励（正分）
        double softReward = 0;
        softReward += calculateTeacherPreferenceScore(chromosome, context);
        softReward += calculateWorkloadBalanceScore(chromosome, context);
        softReward += calculateCourseDistributionScore(chromosome, context);

        // 总适应度 = 奖励 - 惩罚
        double fitness = softReward - hardPenalty;

        chromosome.setFitness(fitness);
        chromosome.setHardConstraintViolations(report.getHardConflicts().size());
        chromosome.setSoftConstraintViolations(report.getSoftConflicts().size());
    }

    /**
     * 根据冲突类型获取惩罚值
     */
    private int getPenalty(Conflict.ConflictType type) {
        return switch (type) {
            case TEACHER_TIME_CONFLICT -> TEACHER_CONFLICT_PENALTY;
            case CLASSROOM_TIME_CONFLICT -> CLASSROOM_CONFLICT_PENALTY;
            case CLASS_TIME_CONFLICT -> CLASS_CONFLICT_PENALTY;
            case CAPACITY_INSUFFICIENT -> CAPACITY_PENALTY;
            case CLASSROOM_TYPE_MISMATCH -> TYPE_MISMATCH_PENALTY;
            case TEACHER_QUALIFICATION_MISMATCH -> QUALIFICATION_PENALTY;
            case EXISTING_SCHEDULE_CONFLICT -> EXISTING_CONFLICT_PENALTY;
            default -> 100_000;
        };
    }

    /**
     * 计算教师时间偏好匹配分数
     */
    private double calculateTeacherPreferenceScore(Chromosome chromosome, ScheduleContext context) {
        if (context.getTeacherTimePreferences() == null || context.getTeacherTimePreferences().isEmpty()) {
            return 0;
        }

        double score = 0;

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : chromosome.getGenes().entrySet()) {
            TimeSlot slot = entry.getKey();

            for (CourseAppointment appt : entry.getValue()) {
                String teacherUuid = appt.getTeacherUuid();
                List<TimeSlot> preferences = context.getTeacherTimePreferences().get(teacherUuid);

                if (preferences != null) {
                    // 检查时间槽是否匹配教师偏好
                    for (TimeSlot preference : preferences) {
                        if (isTimeSlotMatch(slot, preference)) {
                            score += TEACHER_PREFERENCE_REWARD;
                            break;
                        }
                    }
                }
            }
        }

        return score;
    }

    /**
     * 计算教师工作量均衡分数
     */
    private double calculateWorkloadBalanceScore(Chromosome chromosome, ScheduleContext context) {
        if (context.getTeacherMaxHours() == null || context.getTeacherMaxHours().isEmpty()) {
            return 0;
        }

        // 统计每个教师的工作量
        Map<String, Integer> teacherWorkload = new HashMap<>();

        for (List<CourseAppointment> appointments : chromosome.getGenes().values()) {
            for (CourseAppointment appt : appointments) {
                String teacherUuid = appt.getTeacherUuid();
                int hours = appt.getTimeSlot().getTotalHours();
                teacherWorkload.merge(teacherUuid, hours, Integer::sum);
            }
        }

        // 计算均衡分数：教师工作量越接近上限，分数越低
        double score = 0;
        for (Map.Entry<String, Integer> entry : teacherWorkload.entrySet()) {
            String teacherUuid = entry.getKey();
            Integer workload = entry.getValue();
            Integer maxHours = context.getTeacherMaxHours().get(teacherUuid);

            if (maxHours != null && maxHours > 0) {
                double ratio = (double) workload / maxHours;
                // 工作量在合理范围内（< 80%）给奖励
                if (ratio < 0.8) {
                    score += WORKLOAD_BALANCE_REWARD;
                } else if (ratio <= 1.0) {
                    // 80%-100% 给部分奖励
                    score += WORKLOAD_BALANCE_REWARD * (1.0 - ratio);
                }
                // 超过上限不奖励
            }
        }

        return score;
    }

    /**
     * 计算课程分布均匀分数
     */
    private double calculateCourseDistributionScore(Chromosome chromosome, ScheduleContext context) {
        // 统计每天的上课次数
        Map<Integer, Integer> dayDistribution = new HashMap<>();

        for (TimeSlot slot : chromosome.getGenes().keySet()) {
            int day = slot.getDayOfWeek();
            int count = chromosome.getGenes().get(slot).size();
            dayDistribution.merge(day, count, Integer::sum);
        }

        // 计算分布均匀度
        if (dayDistribution.isEmpty()) {
            return 0;
        }

        double avgPerDay = dayDistribution.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        // 计算标准差，越小说明分布越均匀
        double variance = 0;
        for (Integer count : dayDistribution.values()) {
            variance += Math.pow(count - avgPerDay, 2);
        }
        variance /= dayDistribution.size();
        double stdDev = Math.sqrt(variance);

        // 标准差越小，分数越高
        return Math.max(0, COURSE_DISTRIBUTION_REWARD - stdDev * 100);
    }

    /**
     * 判断两个时间槽是否匹配（时间相同，不考虑周次）
     */
    private boolean isTimeSlotMatch(TimeSlot slot1, TimeSlot slot2) {
        return slot1.getDayOfWeek().equals(slot2.getDayOfWeek()) &&
                slot1.getSectionStart().equals(slot2.getSectionStart()) &&
                slot1.getSectionEnd().equals(slot2.getSectionEnd());
    }
}
