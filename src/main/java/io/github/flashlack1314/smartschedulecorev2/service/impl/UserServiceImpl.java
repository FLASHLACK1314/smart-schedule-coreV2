package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import io.github.flashlack1314.smartschedulecorev2.dao.AcademicAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.SystemAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeacherDAO;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SystemAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SystemAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import io.github.flashlack1314.smartschedulecorev2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TokenService tokenService;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final SystemAdminDAO systemAdminDAO;

    @Override
    public UserInfoDTO getUserInfo(String token) {
        // 从 Token 中获取用户信息
        TokenInfoDTO tokenInfoDTO = tokenService.getTokenInfo(token);
        String userUuid = tokenInfoDTO.getUserUuid();
        UserType userType = tokenInfoDTO.getUserType();

        UserInfoDTO result = new UserInfoDTO();

        // 根据用户类型获取对应的用户信息
        switch (userType) {
            case STUDENT:
                StudentDO student = studentDAO.getById(userUuid);
                if (student == null) {
                    throw new BusinessException("学生不存在", ErrorCode.OPERATION_FAILED);
                }
                result.setStudentInfo(buildStudentInfo(student));
                break;

            case TEACHER:
                TeacherDO teacher = teacherDAO.getById(userUuid);
                if (teacher == null) {
                    throw new BusinessException("教师不存在", ErrorCode.OPERATION_FAILED);
                }
                result.setTeacherInfo(buildTeacherInfo(teacher));
                break;

            case ACADEMIC_ADMIN:
                AcademicAdminDO academicAdmin = academicAdminDAO.getById(userUuid);
                if (academicAdmin == null) {
                    throw new BusinessException("教务管理员不存在", ErrorCode.OPERATION_FAILED);
                }
                result.setAcademicAdminInfo(buildAcademicAdminInfo(academicAdmin));
                break;

            case SYSTEM_ADMIN:
                SystemAdminDO systemAdmin = systemAdminDAO.getById(userUuid);
                if (systemAdmin == null) {
                    throw new BusinessException("系统管理员不存在", ErrorCode.OPERATION_FAILED);
                }
                result.setSystemAdminInfo(buildSystemAdminInfo(systemAdmin));
                break;

            default:
                throw new BusinessException("不支持的用户类型", ErrorCode.PARAMETER_INVALID);
        }

        log.info("获取用户信息成功 - 类型: {}, UUID: {}", userType, userUuid);

        return result;
    }

    /**
     * 构建学生信息DTO
     *
     * @param student 学生实体
     * @return 学生信息DTO
     */
    private StudentUserInfoDTO buildStudentInfo(StudentDO student) {
        return new StudentUserInfoDTO(
                student.getStudentUuid(),
                student.getStudentId(),
                student.getStudentName(),
                student.getClassUuid()
        );
    }

    /**
     * 构建教师信息DTO
     *
     * @param teacher 教师实体
     * @return 教师信息DTO
     */
    private TeacherUserInfoDTO buildTeacherInfo(TeacherDO teacher) {
        return new TeacherUserInfoDTO(
                teacher.getTeacherUuid(),
                teacher.getTeacherNum(),
                teacher.getTeacherName(),
                teacher.getTitle(),
                teacher.getMaxHoursPerWeek(),
                teacher.getIsActive(),
                teacher.getLikeTime()
        );
    }

    /**
     * 构建教务管理员信息DTO
     *
     * @param admin 教务管理员实体
     * @return 教务管理员信息DTO
     */
    private AcademicAdminUserInfoDTO buildAcademicAdminInfo(AcademicAdminDO admin) {
        return new AcademicAdminUserInfoDTO(
                admin.getAcademicUuid(),
                admin.getDepartmentUuid(),
                admin.getAcademicNum(),
                admin.getAcademicName()
        );
    }

    /**
     * 构建系统管理员信息DTO
     *
     * @param admin 系统管理员实体
     * @return 系统管理员信息DTO
     */
    private SystemAdminUserInfoDTO buildSystemAdminInfo(SystemAdminDO admin) {
        return new SystemAdminUserInfoDTO(
                admin.getAdminUuid(),
                admin.getAdminUsername()
        );
    }
}
