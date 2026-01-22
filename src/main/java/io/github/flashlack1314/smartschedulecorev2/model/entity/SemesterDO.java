package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学期DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_semester")
public class SemesterDO {

    /**
     * 学期UUID
     */
    @TableId
    private String semesterUuid;

    /**
     * 学期名称
     */
    @TableField("semester_name")
    private String semesterName;
}