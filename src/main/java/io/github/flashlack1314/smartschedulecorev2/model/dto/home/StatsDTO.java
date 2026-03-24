package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计数据DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class StatsDTO {

    /**
     * 本周排课
     */
    private StatItemDTO weeklySchedule;

    /**
     * 活跃教师
     */
    private StatItemDTO activeTeachers;

    /**
     * 学生总数
     */
    private StatItemDTO totalStudents;

    /**
     * 教室使用率
     */
    private StatItemDTO classroomUsage;
}
