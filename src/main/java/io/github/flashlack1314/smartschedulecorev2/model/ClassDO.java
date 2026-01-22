package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 行政班级DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_class")
public class ClassDO {

    /**
     * 行政班级UUID
     */
    @TableId
    private String classUuid;

    /**
     * 专业UUID
     */
    @TableField("major_uuid")
    private String majorUuid;

    /**
     * 行政班级名称
     */
    @TableField("class_name")
    private String className;
}
