package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教务管理员用户信息DTO
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicAdminUserInfoDTO {

    /**
     * 教务人员UUID
     */
    private String academicUuid;

    /**
     * 所属学院UUID
     */
    private String departmentUuid;

    /**
     * 教务工号
     */
    private String academicNum;

    /**
     * 教务名称
     */
    private String academicName;
}