package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加教室类型VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddClassroomTypeVO {
    /**
     * 教室类型UUID（更新时需要）
     */
    private String classroomTypeUuid;
    /**
     * 教室类型名称
     */
    private String classroomTypeName;
}
