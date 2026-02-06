package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加行政班级VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddClassVO {
    /**
     * 行政班级UUID（更新时需要）
     */
    private String classUuid;
    /**
     * 专业UUID
     */
    private String majorUuid;
    /**
     * 行政班级名称
     */
    private String className;
}
