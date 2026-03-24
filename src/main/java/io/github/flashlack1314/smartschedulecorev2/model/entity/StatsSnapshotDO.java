package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计快照DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_stats_snapshot")
public class StatsSnapshotDO {

    /**
     * 快照UUID(主键)
     */
    @TableId
    private String snapshotUuid;

    /**
     * 周开始日期(周一)
     */
    @TableField("week_start_date")
    private LocalDate weekStartDate;

    /**
     * 本周排课数量
     */
    @TableField("weekly_schedule_count")
    private Integer weeklyScheduleCount;

    /**
     * 活跃教师数量
     */
    @TableField("active_teacher_count")
    private Integer activeTeacherCount;

    /**
     * 学生总数
     */
    @TableField("total_student_count")
    private Integer totalStudentCount;

    /**
     * 教室使用率
     */
    @TableField("classroom_usage_rate")
    private BigDecimal classroomUsageRate;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
