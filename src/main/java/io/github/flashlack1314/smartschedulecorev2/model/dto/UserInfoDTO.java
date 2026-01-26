package io.github.flashlack1314.smartschedulecorev2.model.dto;

import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SystemAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherUserInfoDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class UserInfoDTO {
    /**
     * 学生用户信息（仅当用户类型为STUDENT时有值）
     */
    private StudentUserInfoDTO studentInfo;

    /**
     * 教师用户信息（仅当用户类型为TEACHER时有值）
     */
    private TeacherUserInfoDTO teacherInfo;

    /**
     * 教务管理员用户信息（仅当用户类型为ACADEMIC_ADMIN时有值）
     */
    private AcademicAdminUserInfoDTO academicAdminInfo;

    /**
     * 系统管理员用户信息（仅当用户类型为SYSTEM_ADMIN时有值）
     */
    private SystemAdminUserInfoDTO systemAdminInfo;
}
