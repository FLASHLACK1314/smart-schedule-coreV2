package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学楼DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_building")
public class BuildingDO {

    /**
     * 教学楼UUID
     */
    @TableId
    private String buildingUuid;

    /**
     * 教学楼编号
     */
    @TableField("building_num")
    private String buildingNum;

    /**
     * 教学楼名称
     */
    @TableField("building_name")
    private String buildingName;
}
