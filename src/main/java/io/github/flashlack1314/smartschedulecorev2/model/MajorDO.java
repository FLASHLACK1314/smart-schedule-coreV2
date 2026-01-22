package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 专业DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_major")
public class MajorDO {

    /**
     * 专业UUID
     */
    @TableId
    private String majorUuid;

    /**
     * 学院UUID
     */
    @TableField("department_uuid")
    private String departmentUuid;

    /**
     * 专业编号
     */
    @TableField("major_num")
    private String majorNum;

    /**
     * 专业名称
     */
    @TableField("major_name")
    private String majorName;
}