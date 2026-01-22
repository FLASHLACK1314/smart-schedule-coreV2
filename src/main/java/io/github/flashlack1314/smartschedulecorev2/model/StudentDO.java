package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_student")
public class StudentDO {

    /**
     * 学生UUID
     */
    @TableId
    private String studentUuid;

    /**
     * 学号 (唯一编码)
     */
    @TableField("student_id")
    private String studentId;

    /**
     * 学生姓名
     */
    @TableField("student_name")
    private String studentName;

    /**
     * 行政班级UUID (外键)
     */
    @TableField("class_uuid")
    private String classUuid;

    /**
     * 学生密码 (建议加密存储)
     */
    @TableField("student_password")
    private String studentPassword;
}
