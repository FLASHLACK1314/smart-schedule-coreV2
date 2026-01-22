package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生用户信息DTO
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentUserInfoDTO {

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
     * 行政班级UUID
     */
    private String classUuid;
}