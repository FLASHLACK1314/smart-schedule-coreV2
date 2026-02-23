package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排课单元：一个教学班的所有排课安排
 * 一个教学班可能需要多次上课才能完成总学时
 *
 * @author flash
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleUnit {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 教学班信息
     */
    private ScheduleContext.TeachingClassInfo teachingClassInfo;

    /**
     * 该教学班的所有课程安排列表（多次上课）
     */
    private List<CourseAppointment> appointments;

    /**
     * 累计已排课学时
     */
    private Integer scheduledHours;

    /**
     * 是否完成排课
     */
    public boolean isComplete() {
        return scheduledHours >= teachingClassInfo.getCourseTotalHours();
    }

    /**
     * 添加课程安排并更新累计学时
     */
    public void addAppointment(CourseAppointment appointment) {
        this.appointments.add(appointment);
        this.scheduledHours += appointment.getTimeSlot().getTotalHours();
    }
}
