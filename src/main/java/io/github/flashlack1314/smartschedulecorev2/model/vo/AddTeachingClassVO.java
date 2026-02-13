package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加/更新教学班VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddTeachingClassVO {
    /**
     * 教学班UUID（更新时需要）
     */
    private String teachingClassUuid;
    /**
     * 课程UUID
     */
    private String courseUuid;
    /**
     * 教师UUID
     */
    private String teacherUuid;
    /**
     * 学期UUID
     */
    private String semesterUuid;
    /**
     * 教学班名称
     */
    private String teachingClassName;
}
