package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动记录DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_activity_log")
public class ActivityLogDO {

    /**
     * 活动记录UUID(主键)
     */
    @TableId
    private String activityUuid;

    /**
     * 用户UUID
     */
    @TableField("user_uuid")
    private String userUuid;

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 操作类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 操作描述文本
     */
    @TableField("action_text")
    private String actionText;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
