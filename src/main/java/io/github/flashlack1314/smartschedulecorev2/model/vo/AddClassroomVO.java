package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 新增教室信息
 *
 * @author flash
 */
@Getter
@Accessors(chain = true)
public class AddClassroomVO {
    /**
     * 教学楼uuid
     */
    private String buildingUuid;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 教室容量
     */
    private Integer capacity;
    /**
     * 教室类型Uuid
     */
    private String classroomTypeUuid;
}
