package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教室DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_classroom")
public class ClassroomDO {

    /**
     * 教室UUID
     */
    @TableId
    private String classroomUuid;

    /**
     * 教学楼UUID
     */
    @TableField("building_uuid")
    private String buildingUuid;

    /**
     * 教室名称
     */
    @TableField("classroom_name")
    private String classroomName;

    /**
     * 教室容量
     */
    @TableField("classroom_capacity")
    private Integer classroomCapacity;

    /**
     * 教室种类（后端枚举）
     */
    @TableField("classroom_type")
    private String classroomType;
}
