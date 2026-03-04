package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程安排实体：单次上课安排
 *
 * @author flash
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseAppointment {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 教学班名称
     */
    private String teachingClassName;

    /**
     * 课程UUID
     */
    private String courseUuid;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程总学时
     */
    private Integer courseTotalHours;

    /**
     * 教师UUID
     */
    private String teacherUuid;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 教室UUID
     */
    private String classroomUuid;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 教室容量
     */
    private Integer classroomCapacity;

    /**
     * 教室类型UUID
     */
    private String classroomTypeUuid;

    /**
     * 课程类型UUID
     */
    private String courseTypeUuid;

    /**
     * 关联的行政班UUID列表
     */
    private List<String> classUuids;

    /**
     * 学生总人数
     */
    private Integer totalStudents;

    /**
     * 本次安排的时间槽
     */
    private TimeSlot timeSlot;

    /**
     * 深度复制课程安排
     */
    public CourseAppointment copy() {
        CourseAppointment copy = new CourseAppointment();
        copy.setTeachingClassUuid(this.teachingClassUuid);
        copy.setTeachingClassName(this.teachingClassName);
        copy.setCourseUuid(this.courseUuid);
        copy.setCourseName(this.courseName);
        copy.setCourseTotalHours(this.courseTotalHours);
        copy.setTeacherUuid(this.teacherUuid);
        copy.setTeacherName(this.teacherName);
        copy.setClassroomUuid(this.classroomUuid);
        copy.setClassroomName(this.classroomName);
        copy.setClassroomCapacity(this.classroomCapacity);
        copy.setClassroomTypeUuid(this.classroomTypeUuid);
        copy.setCourseTypeUuid(this.courseTypeUuid);
        copy.setClassUuids(this.classUuids != null ? List.copyOf(this.classUuids) : null);
        copy.setTotalStudents(this.totalStudents);

        // 深度复制时间槽
        if (this.timeSlot != null) {
            TimeSlot copiedSlot = new TimeSlot();
            copiedSlot.setDayOfWeek(this.timeSlot.getDayOfWeek());
            copiedSlot.setSectionStart(this.timeSlot.getSectionStart());
            copiedSlot.setSectionEnd(this.timeSlot.getSectionEnd());
            copiedSlot.setWeeks(this.timeSlot.getWeeks() != null ? List.copyOf(this.timeSlot.getWeeks()) : null);
            copy.setTimeSlot(copiedSlot);
        }

        return copy;
    }
}
