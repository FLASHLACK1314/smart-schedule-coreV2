package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 创建公告请求DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class CreateAnnouncementDTO {

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 优先级(HIGH/MEDIUM/LOW)
     */
    private String priority;

    /**
     * 目标用户类型(STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN)
     */
    private String userType;
}
