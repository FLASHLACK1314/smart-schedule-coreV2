package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冲突信息
 *
 * @author flash
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conflict {
    /**
     * 冲突类型
     */
    private ConflictType type;

    /**
     * 冲突严重程度
     */
    private ConflictSeverity severity;

    /**
     * 冲突描述
     */
    private String description;

    /**
     * 涉及的教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 涉及的教师UUID
     */
    private String teacherUuid;

    /**
     * 涉及的教室UUID
     */
    private String classroomUuid;

    /**
     * 冲突类型枚举
     */
    public enum ConflictType {
        /**
         * 教师时间冲突
         */
        TEACHER_TIME_CONFLICT,

        /**
         * 教室时间冲突
         */
        CLASSROOM_TIME_CONFLICT,

        /**
         * 班级时间冲突
         */
        CLASS_TIME_CONFLICT,

        /**
         * 教室容量不足
         */
        CAPACITY_INSUFFICIENT,

        /**
         * 教室类型不匹配
         */
        CLASSROOM_TYPE_MISMATCH,

        /**
         * 教师资格不符
         */
        TEACHER_QUALIFICATION_MISMATCH,

        /**
         * 与已有排课冲突
         */
        EXISTING_SCHEDULE_CONFLICT,

        /**
         * 未完成排课
         */
        INCOMPLETE_SCHEDULE,

        /**
         * 教师时间偏好未满足
         */
        TEACHER_PREFERENCE_NOT_MET,

        /**
         * 教师工作量不均衡
         */
        WORKLOAD_IMBALANCE
    }

    /**
     * 冲突严重程度
     */
    public enum ConflictSeverity {
        /**
         * 硬约束（必须满足）
         */
        HARD,

        /**
         * 软约束（优化目标）
         */
        SOFT
    }
}
