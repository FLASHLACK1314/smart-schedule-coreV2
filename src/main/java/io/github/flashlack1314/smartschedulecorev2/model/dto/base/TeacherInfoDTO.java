package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教师信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class TeacherInfoDTO {
    /**
     * 教师UUID
     */
    private String teacherUuid;

    /**
     * 教师工号
     */
    private String teacherNum;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 职称
     */
    private String title;

    /**
     * 每周最高授课时长
     */
    private Integer maxHoursPerWeek;

    /**
     * 教师时间偏好（JSON字符串格式）
     * 示例：{"1": [3,4]} 表示周一的3-4节课
     */
    private String likeTime;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 所属学院信息
     */
    private DepartmentInfoDTO departmentInfo;
}
