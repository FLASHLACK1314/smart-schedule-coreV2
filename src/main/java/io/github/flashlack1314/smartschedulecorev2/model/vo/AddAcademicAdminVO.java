package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 添加教务管理员VO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AddAcademicAdminVO {
    /**
     * 教务UUID（更新时需要）
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
     * 所属学院UUID
     */
    private String departmentUuid;

    /**
     * 密码（添加时必填，更新时可选）
     */
    private String academicPassword;
}
