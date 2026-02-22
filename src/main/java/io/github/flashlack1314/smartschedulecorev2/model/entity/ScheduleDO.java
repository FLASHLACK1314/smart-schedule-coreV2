package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 排课DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_schedule")
public class ScheduleDO {

    /**
     * 排课记录UUID
     */
    @TableId
    private String scheduleUuid;

    /**
     * 学期UUID
     */
    @TableField("semester_uuid")
    private String semesterUuid;

    /**
     * 教学班UUID (代替原逻辑分组ID)
     */
    @TableField("teaching_class_uuid")
    private String teachingClassUuid;

    /**
     * 课程UUID (冗余字段)
     */
    @TableField("course_uuid")
    private String courseUuid;

    /**
     * 教师UUID (冗余字段)
     */
    @TableField("teacher_uuid")
    private String teacherUuid;

    /**
     * 教室UUID (冗余字段)
     */
    @TableField("classroom_uuid")
    private String classroomUuid;

    /**
     * 星期几 (1-7)
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 起始节次
     */
    @TableField("section_start")
    private Integer sectionStart;

    /**
     * 结束节次
     */
    @TableField("section_end")
    private Integer sectionEnd;

    /**
     * 上课周次 JSON数组字符串 如"[1,2,3]"
     */
    @TableField("weeks_json")
    private String weeksJson;

    /**
     * 累计学时 (单次学时 × 周次数)
     */
    @TableField("credit_hours")
    private Integer creditHours;

    /**
     * 锁定标识：如果老师手动调整并确认了这一节，可以锁定
     */
    @TableField("is_locked")
    private Boolean isLocked;

    /**
     * 状态：0-预览方案, 1-正式执行
     */
    @TableField("status")
    private Integer status;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
