package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 更新学生VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class UpdateStudentVO {
    /**
     * 学生UUID（必填，用于标识要更新的学生）
     */
    private String studentUuid;
    /**
     * 学号
     */
    private String studentId;
    /**
     * 学生姓名
     */
    private String studentName;
    /**
     * 行政班级UUID
     */
    private String classUuid;
    /**
     * 学生密码（可选，不提供则不更新）
     */
    private String studentPassword;
}
