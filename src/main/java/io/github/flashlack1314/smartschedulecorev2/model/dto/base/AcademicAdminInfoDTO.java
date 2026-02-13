package io.github.flashlack1314.smartschedulecorev2.model.dto.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教务管理员信息DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AcademicAdminInfoDTO {
    /**
     * 教务UUID
     */
    private String academicUuid;

    /**
     * 教务工号
     */
    private String academicNum;

    /**
     * 教务名称
     */
    private String academicName;

    /**
     * 所属学院信息
     */
    private DepartmentInfoDTO departmentInfo;
}
