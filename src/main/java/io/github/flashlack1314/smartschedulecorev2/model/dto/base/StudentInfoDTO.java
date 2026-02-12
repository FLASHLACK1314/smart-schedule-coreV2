package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class StudentInfoDTO {
    /**
     * 学生UUID
     */
    private String studentUuid;
    /**
     * 学号
     */
    private String studentId;
    /**
     * 学生姓名
     */
    private String studentName;
    /**
     * 班级信息（包含专业、学院）
     */
    private ClassInfoDTO classInfo;
}
