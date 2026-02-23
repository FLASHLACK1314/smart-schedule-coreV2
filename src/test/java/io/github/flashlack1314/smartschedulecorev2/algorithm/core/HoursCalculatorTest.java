package io.github.flashlack1314.smartschedulecorev2.algorithm.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HoursCalculator 单元测试
 *
 * @author flash
 */
class HoursCalculatorTest {

    private final HoursCalculator calculator = new HoursCalculator();

    @Test
    void testCalculateRequiredSessions_32Hours_2PerSession() {
        int result = calculator.calculateRequiredSessions(32, 2);
        assertEquals(16, result, "32学时，每次2学时，需要16次");
    }

    @Test
    void testCalculateRequiredSessions_33Hours_2PerSession() {
        int result = calculator.calculateRequiredSessions(33, 2);
        assertEquals(17, result, "33学时，每次2学时，需要17次（向上取整）");
    }

    @Test
    void testCalculateRequiredSessions_48Hours_2PerSession() {
        int result = calculator.calculateRequiredSessions(48, 2);
        assertEquals(24, result, "48学时，每次2学时，需要24次");
    }

    @Test
    void testCalculateRequiredSessions_ZeroHours() {
        int result = calculator.calculateRequiredSessions(0, 2);
        assertEquals(0, result, "0学时应该返回0次");
    }

    @Test
    void testCalculateRequiredSessions_NullHours() {
        int result = calculator.calculateRequiredSessions(null, 2);
        assertEquals(0, result, "null学时应该返回0次");
    }

    @Test
    void testCalculateRequiredWeeks_16Sessions_1PerWeek() {
        int result = calculator.calculateRequiredWeeks(16, 1);
        assertEquals(16, result, "16次课，每周1次，需要16周");
    }

    @Test
    void testCalculateRequiredWeeks_16Sessions_2PerWeek() {
        int result = calculator.calculateRequiredWeeks(16, 2);
        assertEquals(8, result, "16次课，每周2次，需要8周");
    }

    @Test
    void testCalculateRequiredWeeks_17Sessions_2PerWeek() {
        int result = calculator.calculateRequiredWeeks(17, 2);
        assertEquals(9, result, "17次课，每周2次，需要9周（向上取整）");
    }

    @Test
    void testGenerateWeekDistribution_8Weeks_16Semester() {
        List<Integer> weeks = calculator.generateWeekDistribution(8, 16);
        assertEquals(8, weeks.size(), "应该生成8个周次");
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), weeks, "应该从前8周开始连续分配");
    }

    @Test
    void testGenerateWeekDistribution_20Weeks_16Semester() {
        List<Integer> weeks = calculator.generateWeekDistribution(20, 16);
        assertEquals(16, weeks.size(), "超过学期周数时，应该返回所有周");
    }

    @Test
    void testCalculateCreditHours() {
        int result = calculator.calculateCreditHours(2, 8);
        assertEquals(16, result, "2学时 × 8周 = 16学时");
    }
}
