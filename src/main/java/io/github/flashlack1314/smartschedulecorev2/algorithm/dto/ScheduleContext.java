package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 自动排课上下文：封装算法运行所需的所有输入数据
 *
 * @author flash
 */
@Data
public class ScheduleContext {
    /**
     * 学期UUID
     */
    private String semesterUuid;

    /**
     * 学期周数
     */
    private Integer semesterWeeks;

    /**
     * 学期开始日期
     */
    private LocalDate startDate;

    /**
     * 学期结束日期
     */
    private LocalDate endDate;

    /**
     * 待排课的教学班列表
     */
    private List<TeachingClassInfo> teachingClassList;

    /**
     * 可用教室列表 (按教室类型分组)
     */
    private Map<String, List<ClassroomInfo>> availableClassrooms;

    /**
     * 教师时间偏好 (teacherUuid -> List<TimeSlot>)
     */
    private Map<String, List<TimeSlot>> teacherTimePreferences;

    /**
     * 教师每周最大课时 (teacherUuid -> maxHours)
     */
    private Map<String, Integer> teacherMaxHours;

    /**
     * 已有的正式排课记录 (status=1，用于冲突检测)
     */
    private List<ExistingSchedule> existingSchedules;

    /**
     * 每周节次数 (默认12节，即6个2节时段)
     */
    private Integer sectionsPerDay = 12;

    /**
     * 每周上课天数 (默认5天)
     */
    private Integer daysPerWeek = 5;

    /**
     * 单次上课学时 (固定2)
     */
    private Integer hoursPerSession = 2;

    /**
     * 课程-行政班映射（用于合班上课约束）
     * key: 课程UUID
     * value: 该课程对应的行政班UUID列表
     *
     * 用于确保同一课程对应的多个行政班在同一时间上课
     */
    private Map<String, List<String>> courseClassMapping;

    /**
     * 课程类型 -> 可用教室类型列表的映射
     * key: 课程类型UUID (sc_course_type.course_type_uuid)
     * value: 该课程类型可用的教室类型UUID列表 (sc_classroom_type.classroom_type_uuid)
     *
     * 从 sc_course_classroom_type 关联表加载，用于确保课程被安排到合适类型的教室
     */
    private Map<String, List<String>> courseTypeToClassroomTypes;

    /**
     * 已有排课记录信息
     */
    @Data
    public static class ExistingSchedule {
        private String scheduleUuid;
        private String teachingClassUuid;
        private String teacherUuid;
        private String classroomUuid;
        private List<String> classUuids;
        private TimeSlot timeSlot;
    }

    /**
     * 教学班信息
     */
    @Data
    public static class TeachingClassInfo {
        private String teachingClassUuid;
        private String teachingClassName;
        private String courseUuid;
        private String courseName;
        private String courseTypeUuid;
        private Integer courseTotalHours;
        private String teacherUuid;
        private String teacherName;
        private List<String> classUuids;
        private Integer totalStudents;
        private Integer weeklySessions;
        private Integer sectionsPerSession;
        private Integer requiredSessions;
        /**
         * 需要的上课周数
         * 公式: ceil(总学时 / (每周次数 × 每次学时))
         * 例如: 48学时, 每周2次, 每次2节 -> ceil(48/4) = 12周
         */
        private Integer requiredWeeks;
    }

    /**
     * 教室信息
     */
    @Data
    public static class ClassroomInfo {
        private String classroomUuid;
        private String classroomName;
        private Integer capacity;
        private String classroomTypeUuid;
    }
}
