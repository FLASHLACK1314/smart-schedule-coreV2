package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统公告DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_announcement")
public class AnnouncementDO {

    /**
     * 公告UUID(主键)
     */
    @TableId
    private String announcementUuid;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 公告内容
     */
    @TableField("content")
    private String content;

    /**
     * 优先级(HIGH/MEDIUM/LOW)
     */
    @TableField("priority")
    private String priority;

    /**
     * 目标用户类型(STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN)
     */
    @TableField("user_type")
    private String userType;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
