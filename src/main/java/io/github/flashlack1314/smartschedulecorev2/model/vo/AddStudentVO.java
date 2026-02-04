package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加学生VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddStudentVO {
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
     * 学生密码
     */
    private String studentPassword;
}
