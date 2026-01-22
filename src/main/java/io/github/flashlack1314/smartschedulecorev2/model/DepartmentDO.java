package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学院DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_department")
public class DepartmentDO {

    /**
     * 学院UUID
     */
    @TableId
    private String departmentUuid;

    /**
     * 学院名称
     */
    @TableField("department_name")
    private String departmentName;
}