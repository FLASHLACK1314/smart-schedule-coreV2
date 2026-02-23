package io.github.flashlack1314.smartschedulecorev2.algorithm.util;

import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 时间槽生成器：生成所有可能的时间槽
 *
 * @author flash
 */
@Component
public class TimeSlotGenerator {

    private final Random random = new Random();

    /**
     * 生成所有可能的时间槽
     * 固定2节连上：1-2, 3-4, 5-6, 7-8, 9-10, 11-12
     *
     * @param daysPerWeek    每周上课天数
     * @param sectionsPerDay 每天节次数
     * @param weeks          上课周次列表
     * @return 所有可能的时间槽列表
     */
    public List<TimeSlot> generateAllTimeSlots(Integer daysPerWeek, Integer sectionsPerDay, List<Integer> weeks) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        // 设置默认值
        if (daysPerWeek == null || daysPerWeek <= 0) {
            daysPerWeek = 5;
        }
        if (sectionsPerDay == null || sectionsPerDay <= 0) {
            sectionsPerDay = 12;
        }
        if (weeks == null || weeks.isEmpty()) {
            weeks = List.of(1);
        }

        // 每周上课天数
        for (int day = 1; day <= daysPerWeek; day++) {
            // 每天 sectionsPerDay / 2 个时段（每个时段2节）
            for (int section = 1; section <= sectionsPerDay; section += 2) {
                TimeSlot slot = new TimeSlot();
                slot.setDayOfWeek(day);
                slot.setSectionStart(section);
                slot.setSectionEnd(section + 1);
                slot.setWeeks(new ArrayList<>(weeks));
                timeSlots.add(slot);
            }
        }

        return timeSlots;
    }

    /**
     * 生成单个随机时间槽
     *
     * @param availableSlots 可用时间槽列表
     * @return 随机选择的时间槽，如果列表为空则返回null
     */
    public TimeSlot generateRandomTimeSlot(List<TimeSlot> availableSlots) {
        if (availableSlots == null || availableSlots.isEmpty()) {
            return null;
        }
        return availableSlots.get(random.nextInt(availableSlots.size()));
    }

    /**
     * 从可用时间槽中排除指定的时间槽
     *
     * @param availableSlots 可用时间槽列表
     * @param excludedSlots  要排除的时间槽列表
     * @return 过滤后的时间槽列表
     */
    public List<TimeSlot> excludeSlots(List<TimeSlot> availableSlots, List<TimeSlot> excludedSlots) {
        if (excludedSlots == null || excludedSlots.isEmpty()) {
            return availableSlots;
        }

        return availableSlots.stream()
                .filter(slot -> excludedSlots.stream()
                        .noneMatch(excluded -> isSameSlot(slot, excluded)))
                .toList();
    }

    /**
     * 判断两个时间槽是否是同一个时间槽（时间相同，不考虑周次）
     */
    private boolean isSameSlot(TimeSlot slot1, TimeSlot slot2) {
        return slot1.getDayOfWeek().equals(slot2.getDayOfWeek()) &&
                slot1.getSectionStart().equals(slot2.getSectionStart()) &&
                slot1.getSectionEnd().equals(slot2.getSectionEnd());
    }
}
