package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加教师VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddTeacherVO {
    /**
     * 教师UUID（更新时需要）
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
     * 所属学院UUID
     */
    private String departmentUuid;

    /**
     * 密码（添加时必填，更新时可选）
     */
    private String teacherPassword;

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
}
