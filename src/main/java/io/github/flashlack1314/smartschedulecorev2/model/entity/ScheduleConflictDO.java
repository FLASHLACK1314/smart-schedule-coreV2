package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 排课冲突记录DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_schedule_conflict")
public class ScheduleConflictDO {

    /**
     * 冲突记录UUID
     */
    @TableId
    private String conflictUuid;

    /**
     * 学期UUID
     */
    @TableField("semester_uuid")
    private String semesterUuid;

    /**
     * 排课记录A的UUID
     */
    @TableField("schedule_uuid_a")
    private String scheduleUuidA;

    /**
     * 排课记录B的UUID
     */
    @TableField("schedule_uuid_b")
    private String scheduleUuidB;

    /**
     * 冲突类型
     */
    @TableField("conflict_type")
    private String conflictType;

    /**
     * 严重程度 (1:硬冲突, 0:软冲突)
     */
    @TableField("severity")
    private Integer severity;

    /**
     * 冲突描述
     */
    @TableField("description")
    private String description;
}
