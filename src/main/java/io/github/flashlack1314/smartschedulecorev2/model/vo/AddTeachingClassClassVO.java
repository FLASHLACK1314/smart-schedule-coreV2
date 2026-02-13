package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加教学班-行政班关联VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddTeachingClassClassVO {
    /**
     * 教学班UUID
     */
    private String teachingClassUuid;

    /**
     * 行政班级UUID
     */
    private String classUuid;
}
