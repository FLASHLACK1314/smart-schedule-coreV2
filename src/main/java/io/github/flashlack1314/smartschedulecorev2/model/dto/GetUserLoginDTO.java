package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录返回信息
 *
 * @author flash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserLoginDTO {

    /**
     * 用户类型（STUDENT, TEACHER, ACADEMIC_ADMIN, SYSTEM_ADMIN）
     */
    private String userType;

    /**
     * Token
     */
    private String token;

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
