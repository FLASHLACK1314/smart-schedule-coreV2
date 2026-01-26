package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学班-行政班级关联DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_teaching_class_class")
public class TeachingClassClassDO {

    /**
     * 关联关系UUID
     */
    @TableId
    private String teachingClassClassUuid;

    /**
     * 教学班UUID
     */
    @TableField("teaching_class_uuid")
    private String teachingClassUuid;

    /**
     * 行政班级UUID
     */
    @TableField("class_uuid")
    private String classUuid;
}