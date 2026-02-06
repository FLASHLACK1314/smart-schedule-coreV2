package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 行政班级信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class ClassInfoDTO {
    /**
     * 行政班级UUID
     */
    private String classUuid;
    /**
     * 行政班级名称
     */
    private String className;
    /**
     * 专业信息
     */
    private MajorInfoDTO majorInfo;
}
