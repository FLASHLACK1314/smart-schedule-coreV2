package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师用户信息DTO
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUserInfoDTO {
    /**
     * 教师UUID
     */
    private String teacherUuid;

    /**
     * 教师编号
     */
    private String teacherNum;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 职称
     */
    private String title;

    /**
     * 所属学院UUID
     */
    private String departmentUuid;

    /**
     * 学院名称
     */
    private String departmentName;
    /**
     * 每周最高授课时长
     */
    private Integer maxHoursPerWeek;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 喜欢时间 (JSONB 格式)
     */
    private String likeTime;
}