package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeSlot 单元测试
 *
 * @author flash
 */
class TimeSlotTest {

    @Test
    void testIsOverlap_SameDaySameTime_ShouldReturnTrue() {
        TimeSlot slot1 = new TimeSlot(1, 1, 2, List.of(1, 2, 3));
        TimeSlot slot2 = new TimeSlot(1, 1, 2, List.of(3, 4, 5));

        assertTrue(slot1.isOverlap(slot2), "同一时间有重叠周次应该返回true");
    }

    @Test
    void testIsOverlap_SameDayDifferentTime_ShouldReturnFalse() {
        TimeSlot slot1 = new TimeSlot(1, 1, 2, List.of(1, 2, 3));
        TimeSlot slot2 = new TimeSlot(1, 3, 4, List.of(2, 3, 4));

        assertFalse(slot1.isOverlap(slot2), "同一天不同时间应该返回false");
    }

    @Test
    void testIsOverlap_DifferentDay_ShouldReturnFalse() {
        TimeSlot slot1 = new TimeSlot(1, 1, 2, List.of(1, 2, 3));
        TimeSlot slot2 = new TimeSlot(2, 1, 2, List.of(1, 2, 3));

        assertFalse(slot1.isOverlap(slot2), "不同星期几应该返回false");
    }

    @Test
    void testIsOverlap_SameDayOverlappingSection_ShouldReturnTrue() {
        TimeSlot slot1 = new TimeSlot(1, 1, 2, List.of(1, 2));
        TimeSlot slot2 = new TimeSlot(1, 2, 3, List.of(2, 3));

        // 这里要注意：我们固定是2节连上，sectionEnd = sectionStart + 1
        // 所以 sectionStart=1, sectionEnd=2 和 sectionStart=2, sectionEnd=3 是不重叠的
        // 因为第一个是第1-2节，第二个是第2-3节
        // 实际上在我们的设计中，sectionStart 总是单数，所以不会有这种情况
        assertTrue(slot1.isOverlap(slot2), "节次有重叠且周次有重叠应该返回true");
    }

    @Test
    void testIsOverlap_SameDaySameSectionNoWeekOverlap_ShouldReturnFalse() {
        TimeSlot slot1 = new TimeSlot(1, 1, 2, List.of(1, 2));
        TimeSlot slot2 = new TimeSlot(1, 1, 2, List.of(3, 4));

        assertFalse(slot1.isOverlap(slot2), "同一时间但周次不重叠应该返回false");
    }

    @Test
    void testGetTotalHours_2Weeks_ShouldReturn4() {
        TimeSlot slot = new TimeSlot(1, 1, 2, List.of(1, 2));

        assertEquals(4, slot.getTotalHours(), "2周 × 2学时 = 4学时");
    }

    @Test
    void testGetTotalHours_5Weeks_ShouldReturn10() {
        TimeSlot slot = new TimeSlot(1, 3, 4, List.of(1, 2, 3, 4, 5));

        assertEquals(10, slot.getTotalHours(), "5周 × 2学时 = 10学时");
    }

    @Test
    void testGetUniqueId() {
        TimeSlot slot = new TimeSlot(1, 1, 2, List.of(1, 2, 3));

        String uniqueId = slot.getUniqueId();
        assertTrue(uniqueId.contains("1-"), "ID应该包含星期几");
        assertTrue(uniqueId.contains("-1-"), "ID应该包含起始节次");
        assertTrue(uniqueId.contains("-2-"), "ID应该包含结束节次");
        assertTrue(uniqueId.contains("[1, 2, 3]"), "ID应该包含周次列表");
    }

    @Test
    void testHoursPerSessionConstant() {
        assertEquals(2, TimeSlot.HOURS_PER_SESSION, "单次上课固定为2学时");
    }
}
