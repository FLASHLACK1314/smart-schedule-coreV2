package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加学院VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddDepartmentVO {
    /**
     * 学院UUID（更新时需要）
     */
    private String departmentUuid;
    /**
     * 学院名称
     */
    private String departmentName;
}
