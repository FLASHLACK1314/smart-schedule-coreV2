package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.PasswordUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.GetUserLoginDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SystemAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.model.vo.ChangePasswordVO;
import io.github.flashlack1314.smartschedulecorev2.service.AuthService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TokenService tokenService;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final SystemAdminDAO systemAdminDAO;
    private final ClassDAO classDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public GetUserLoginDTO login(String userType, String userName, String password) {
        UserType type = UserType.fromString(userType);
        String userUuid;
        GetUserLoginDTO result = new GetUserLoginDTO();
        result.setUserType(type.name());

        switch (type) {
            case STUDENT:
                StudentDO student = authenticateStudent(userName, password);
                userUuid = student.getStudentUuid();
                result.setStudentInfo(buildStudentInfo(student));
                break;

            case TEACHER:
                TeacherDO teacher = authenticateTeacher(userName, password);
                userUuid = teacher.getTeacherUuid();
                result.setTeacherInfo(buildTeacherInfo(teacher));
                break;

            case ACADEMIC_ADMIN:
                AcademicAdminDO academicAdmin = authenticateAcademicAdmin(userName, password);
                userUuid = academicAdmin.getAcademicUuid();
                result.setAcademicAdminInfo(buildAcademicAdminInfo(academicAdmin));
                break;

            case SYSTEM_ADMIN:
                SystemAdminDO systemAdmin = authenticateSystemAdmin(userName, password);
                userUuid = systemAdmin.getAdminUuid();
                result.setSystemAdminInfo(buildSystemAdminInfo(systemAdmin));
                break;

            default:
                throw new BusinessException("不支持的用户类型", ErrorCode.PARAMETER_INVALID);
        }

        // 生成Token
        String token = tokenService.generateToken(userUuid, type);
        result.setToken(token);

        log.info("用户登录成功 - 类型: {}, 用户名: {}, UUID: {}", type, userName, userUuid);

        return result;
    }

    @Override
    public void logout(String token) {
        tokenService.deleteToken(token);
        log.info("用户退出登录成功");
    }

    @Override
    public void changePassword(ChangePasswordVO getData, String token) {
        // 验证新密码和确认密码是否一致
        if (!getData.getNewPassword().equals(getData.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致", ErrorCode.PARAMETER_INVALID);
        }

        // 从 Token 中获取用户信息
        TokenInfoDTO tokenInfoDTO = tokenService.getTokenInfo(token);
        String userUuid = tokenInfoDTO.getUserUuid();
        UserType userType = tokenInfoDTO.getUserType();

        // 系统管理员不允许修改密码
        if (userType == UserType.SYSTEM_ADMIN) {
            throw new BusinessException("系统管理员不允许修改密码", ErrorCode.OPERATION_DENIED);
        }

        // 根据用户类型修改密码
        switch (userType) {
            case STUDENT:
                this.changeStudentPassword(userUuid, getData.getNewPassword());
                break;

            case TEACHER:
                changeTeacherPassword(userUuid, getData.getNewPassword());
                break;

            case ACADEMIC_ADMIN:
                changeAcademicAdminPassword(userUuid, getData.getNewPassword());
                break;

            default:
                throw new BusinessException("不支持的用户类型", ErrorCode.PARAMETER_INVALID);
        }

        log.info("用户密码修改成功 - 类型: {}, UUID: {}", userType, userUuid);
    }

    /**
     * 验证学生身份
     *
     * @param studentId 学号
     * @param password  密码
     * @return 学生信息
     */
    private StudentDO authenticateStudent(String studentId, String password) {
        LambdaQueryWrapper<StudentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentDO::getStudentId, studentId);
        StudentDO student = studentDAO.getOne(wrapper);

        if (student == null) {
            throw new BusinessException("学号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        if (!PasswordUtil.verify(password, student.getStudentPassword())) {
            throw new BusinessException("学号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        return student;
    }

    /**
     * 验证教师身份
     *
     * @param teacherNum 教师工号
     * @param password   密码
     * @return 教师信息
     */
    private TeacherDO authenticateTeacher(String teacherNum, String password) {
        LambdaQueryWrapper<TeacherDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherDO::getTeacherNum, teacherNum);
        TeacherDO teacher = teacherDAO.getOne(wrapper);

        if (teacher == null) {
            throw new BusinessException("工号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        if (!PasswordUtil.verify(password, teacher.getTeacherPassword())) {
            throw new BusinessException("工号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        if (!teacher.getIsActive()) {
            throw new BusinessException("该教师账号已被禁用", ErrorCode.OPERATION_DENIED);
        }

        return teacher;
    }

    /**
     * 验证教务管理员身份
     *
     * @param academicNum 教务工号
     * @param password    密码
     * @return 教务管理员信息
     */
    private AcademicAdminDO authenticateAcademicAdmin(String academicNum, String password) {
        LambdaQueryWrapper<AcademicAdminDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AcademicAdminDO::getAcademicNum, academicNum);
        AcademicAdminDO academicAdmin = academicAdminDAO.getOne(wrapper);

        if (academicAdmin == null) {
            throw new BusinessException("工号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        if (!PasswordUtil.verify(password, academicAdmin.getAcademicPassword())) {
            throw new BusinessException("工号或密码错误", ErrorCode.OPERATION_FAILED);
        }

        return academicAdmin;
    }

    /**
     * 验证系统管理员身份
     *
     * @param username 用户名
     * @param password 密码
     * @return 系统管理员信息
     */
    private SystemAdminDO authenticateSystemAdmin(String username, String password) {
        LambdaQueryWrapper<SystemAdminDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemAdminDO::getAdminUsername, username);
        SystemAdminDO systemAdmin = systemAdminDAO.getOne(wrapper);

        if (systemAdmin == null) {
            throw new BusinessException("用户名或密码错误", ErrorCode.OPERATION_FAILED);
        }

        if (!PasswordUtil.verify(password, systemAdmin.getAdminPassword())) {
            throw new BusinessException("用户名或密码错误", ErrorCode.OPERATION_FAILED);
        }

        return systemAdmin;
    }

    /**
     * 构建学生信息DTO
     *
     * @param student 学生实体
     * @return 学生信息DTO
     */
    private StudentUserInfoDTO buildStudentInfo(StudentDO student) {
        // 查询班级名称
        String className = null;
        if (student.getClassUuid() != null) {
            ClassDO classDO = classDAO.getById(student.getClassUuid());
            if (classDO != null) {
                className = classDO.getClassName();
            } else {
                log.warn("学生关联的班级不存在 - 学生UUID: {}, 班级UUID: {}",
                        student.getStudentUuid(), student.getClassUuid());
            }
        }

        return new StudentUserInfoDTO(
                student.getStudentUuid(),
                student.getStudentId(),
                student.getStudentName(),
                student.getClassUuid(),
                className
        );
    }

    /**
     * 构建教师信息DTO
     *
     * @param teacher 教师实体
     * @return 教师信息DTO
     */
    private TeacherUserInfoDTO buildTeacherInfo(TeacherDO teacher) {
        // 查询学院名称
        String departmentName = null;
        if (teacher.getDepartmentUuid() != null) {
            DepartmentDO departmentDO = departmentDAO.getById(teacher.getDepartmentUuid());
            if (departmentDO != null) {
                departmentName = departmentDO.getDepartmentName();
            } else {
                log.warn("教师关联的学院不存在 - 教师UUID: {}, 学院UUID: {}",
                        teacher.getTeacherUuid(), teacher.getDepartmentUuid());
            }
        }

        return new TeacherUserInfoDTO(
                teacher.getTeacherUuid(),
                teacher.getTeacherNum(),
                teacher.getTeacherName(),
                teacher.getTitle(),
                teacher.getDepartmentUuid(),
                departmentName,
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
        // 查询学院名称
        String departmentName = null;
        if (admin.getDepartmentUuid() != null) {
            DepartmentDO departmentDO = departmentDAO.getById(admin.getDepartmentUuid());
            if (departmentDO != null) {
                departmentName = departmentDO.getDepartmentName();
            } else {
                log.warn("教务管理员关联的学院不存在 - 教务UUID: {}, 学院UUID: {}",
                        admin.getAcademicUuid(), admin.getDepartmentUuid());
            }
        }

        return new AcademicAdminUserInfoDTO(
                admin.getAcademicUuid(),
                admin.getDepartmentUuid(),
                departmentName,
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

    /**
     * 修改学生密码
     *
     * @param studentUuid 学生UUID
     * @param newPassword  新密码
     */
    private void changeStudentPassword(String studentUuid, String newPassword) {
        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            throw new BusinessException("学生不存在", ErrorCode.OPERATION_FAILED);
        }

        student.setStudentPassword(PasswordUtil.encrypt(newPassword));
        studentDAO.updateById(student);

        log.info("学生密码修改成功 - UUID: {}, 学号: {}", studentUuid, student.getStudentId());
    }

    /**
     * 修改教师密码
     *
     * @param teacherUuid 教师UUID
     * @param newPassword  新密码
     */
    private void changeTeacherPassword(String teacherUuid, String newPassword) {
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        if (teacher == null) {
            throw new BusinessException("教师不存在", ErrorCode.OPERATION_FAILED);
        }

        teacher.setTeacherPassword(PasswordUtil.encrypt(newPassword));
        teacherDAO.updateById(teacher);

        log.info("教师密码修改成功 - UUID: {}, 工号: {}", teacherUuid, teacher.getTeacherNum());
    }

    /**
     * 修改教务管理员密码
     *
     * @param academicUuid 教务管理员UUID
     * @param newPassword   新密码
     */
    private void changeAcademicAdminPassword(String academicUuid, String newPassword) {
        AcademicAdminDO admin = academicAdminDAO.getById(academicUuid);
        if (admin == null) {
            throw new BusinessException("教务管理员不存在", ErrorCode.OPERATION_FAILED);
        }

        admin.setAcademicPassword(PasswordUtil.encrypt(newPassword));
        academicAdminDAO.updateById(admin);

        log.info("教务管理员密码修改成功 - UUID: {}, 工号: {}", academicUuid, admin.getAcademicNum());
    }
}