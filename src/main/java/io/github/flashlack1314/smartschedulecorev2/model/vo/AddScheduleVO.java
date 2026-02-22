package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加/更新排课VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddScheduleVO {
    /**
     * 排课记录UUID（更新时需要）
     */
    private String scheduleUuid;

    /**
     * 学期UUID（必填）
     */
    private String semesterUuid;

    /**
     * 教学班UUID（必填）
     */
    private String teachingClassUuid;

    /**
     * 教室UUID（必填）
     */
    private String classroomUuid;

    /**
     * 星期几 (1-7)（必填）
     */
    private Integer dayOfWeek;

    /**
     * 起始节次（必填）
     */
    private Integer sectionStart;

    /**
     * 结束节次（必填）
     */
    private Integer sectionEnd;

    /**
     * 上课周次，JSON数组字符串，如 "[1,2,3,4,5]"（必填）
     */
    private String weeksJson;

    /**
     * 状态：0-预览方案, 1-正式执行（必填）
     */
    private Integer status;
}
