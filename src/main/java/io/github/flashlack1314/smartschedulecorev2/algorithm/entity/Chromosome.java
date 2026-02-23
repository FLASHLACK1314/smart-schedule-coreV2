package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 染色体：表示一个完整的排课方案
 * 使用 Map<TimeSlot, List<CourseAppointment>> 编码
 *
 * @author flash
 */
@Data
public class Chromosome implements Comparable<Chromosome> {
    /**
     * 排课方案：时间槽 -> 该时间段的所有课程安排
     */
    private Map<TimeSlot, List<CourseAppointment>> genes;

    /**
     * 适应度分数 (越大越好)
     */
    private Double fitness;

    /**
     * 硬约束违反次数
     */
    private int hardConstraintViolations;

    /**
     * 软约束违反次数
     */
    private int softConstraintViolations;

    /**
     * 未完成排课的教学班列表
     */
    private List<String> unscheduledTeachingClasses;

    public Chromosome() {
        this.genes = new HashMap<>();
        this.fitness = 0.0;
        this.hardConstraintViolations = 0;
        this.softConstraintViolations = 0;
        this.unscheduledTeachingClasses = new ArrayList<>();
    }

    /**
     * 深度复制染色体
     */
    public Chromosome copy() {
        Chromosome copy = new Chromosome();
        Map<TimeSlot, List<CourseAppointment>> copiedGenes = new HashMap<>();

        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : genes.entrySet()) {
            List<CourseAppointment> appointments = new ArrayList<>();
            for (CourseAppointment appt : entry.getValue()) {
                appointments.add(appt.copy());
            }

            // 深度复制时间槽
            TimeSlot originalSlot = entry.getKey();
            TimeSlot copiedSlot = new TimeSlot();
            copiedSlot.setDayOfWeek(originalSlot.getDayOfWeek());
            copiedSlot.setSectionStart(originalSlot.getSectionStart());
            copiedSlot.setSectionEnd(originalSlot.getSectionEnd());
            copiedSlot.setWeeks(originalSlot.getWeeks() != null ? List.copyOf(originalSlot.getWeeks()) : null);

            copiedGenes.put(copiedSlot, appointments);
        }

        copy.setGenes(copiedGenes);
        copy.setFitness(this.fitness);
        copy.setHardConstraintViolations(this.hardConstraintViolations);
        copy.setSoftConstraintViolations(this.softConstraintViolations);
        copy.setUnscheduledTeachingClasses(new ArrayList<>(this.unscheduledTeachingClasses));

        return copy;
    }

    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(other.fitness, this.fitness);
    }
}
