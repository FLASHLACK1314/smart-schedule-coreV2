package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加课程类型VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddCourseTypeVO {
    /**
     * 课程类型UUID（更新时需要）
     */
    private String courseTypeUuid;
    /**
     * 课程类型名称
     */
    private String courseTypeName;
}
