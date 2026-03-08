package io.github.flashlack1314.smartschedulecorev2.algorithm.core;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 学时计算器：负责计算和分配学时
 *
 * @author flash
 */
@Component
public class HoursCalculator {

    /**
     * 计算教学班需要的上课次数
     * 公式：ceil(courseTotalHours / hoursPerSession)
     *
     * @param courseTotalHours 课程总学时
     * @param hoursPerSession  每次上课学时（固定为2）
     * @return 需要的上课次数
     */
    public int calculateRequiredSessions(Integer courseTotalHours, Integer hoursPerSession) {
        if (courseTotalHours == null || courseTotalHours <= 0) {
            return 0;
        }
        if (hoursPerSession == null || hoursPerSession <= 0) {
            hoursPerSession = 2; // 默认值
        }
        return (int) Math.ceil((double) courseTotalHours / hoursPerSession);
    }

    /**
     * 计算需要的周数范围
     * 如果每周上课weeklySessions次，共需要requiredSessions次
     * 则需要 ceil(requiredSessions / weeklySessions) 周
     *
     * @param requiredSessions 需要的上课次数
     * @param weeklySessions   每周上课次数
     * @return 需要的周数
     */
    public int calculateRequiredWeeks(int requiredSessions, int weeklySessions) {
        if (weeklySessions <= 0) {
            weeklySessions = 1; // 默认值
        }
        return (int) Math.ceil((double) requiredSessions / weeklySessions);
    }

    /**
     * 计算课程需要的上课周数（新公式）
     * 公式: ceil(总学时 / (每周次数 × 每次学时))
     *
     * @param courseTotalHours 课程总学时
     * @param weeklySessions   每周上课次数
     * @param hoursPerSession  每次上课学时（固定为2）
     * @return 需要的周数
     */
    public int calculateRequiredWeeks(Integer courseTotalHours, Integer weeklySessions, Integer hoursPerSession) {
        if (courseTotalHours == null || courseTotalHours <= 0) {
            return 0;
        }
        if (weeklySessions == null || weeklySessions <= 0) {
            weeklySessions = 1; // 默认每周1次
        }
        if (hoursPerSession == null || hoursPerSession <= 0) {
            hoursPerSession = 2; // 默认每次2节
        }
        // 每周总学时 = 每周次数 × 每次学时
        int weeklyHours = weeklySessions * hoursPerSession;
        // 需要的周数 = ceil(总学时 / 每周总学时)
        return (int) Math.ceil((double) courseTotalHours / weeklyHours);
    }

    /**
     * 为教学班生成周次分配方案
     * 算法：均匀分布在学期周次中
     *
     * @param requiredWeeks 需要的周数
     * @param semesterWeeks 学期总周数
     * @return 周次列表
     */
    public List<Integer> generateWeekDistribution(int requiredWeeks, int semesterWeeks) {
        if (requiredWeeks <= 0 || semesterWeeks <= 0) {
            return List.of();
        }

        // 如果需要的周数大于等于学期周数，返回所有周
        if (requiredWeeks >= semesterWeeks) {
            return IntStream.rangeClosed(1, semesterWeeks)
                    .boxed()
                    .collect(Collectors.toList());
        }

        // 简单实现：从第1周开始连续分配requiredWeeks周
        // TODO: 可以实现间隔分配、单双周等策略
        return IntStream.rangeClosed(1, Math.min(requiredWeeks, semesterWeeks))
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 计算累计学时
     *
     * @param sessionHours 单次学时
     * @param weeksCount   周次数
     * @return 总学时
     */
    public int calculateCreditHours(int sessionHours, int weeksCount) {
        return sessionHours * weeksCount;
    }
}
