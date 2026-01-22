package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教务管理DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_academic_admin")
public class AcademicAdminDO {

    /**
     * 教务人员UUID
     */
    @TableId
    private String academicUuid;

    /**
     * 所属学院UUID (外键)
     */
    @TableField("department_uuid")
    private String departmentUuid;

    /**
     * 教务工号 (唯一编码)
     */
    @TableField("academic_num")
    private String academicNum;

    /**
     * 教务名称
     */
    @TableField("academic_name")
    private String academicName;

    /**
     * 教务密码 (建议加密存储)
     */
    @TableField("academic_password")
    private String academicPassword;
}
