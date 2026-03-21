package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Dify会话关联表DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_dify_conversation")
public class DifyConversationDO {

    /**
     * 会话记录UUID(主键)
     */
    @TableId
    private String conversationUuid;

    /**
     * 用户UUID
     */
    @TableField("user_uuid")
    private String userUuid;

    /**
     * 用户类型(STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN)
     */
    @TableField("user_type")
    private String userType;

    /**
     * Dify会话ID
     */
    @TableField("dify_conversation_id")
    private String difyConversationId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 最后对话时间(用于获取最新会话)
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
