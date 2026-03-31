package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.AcademicAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ActivityLogDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeacherDAO;
import io.github.flashlack1314.smartschedulecorev2.enums.ActionType;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.service.ActivityLogService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 活动日志服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogDAO activityLogDAO;
    private final TokenService tokenService;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final AcademicAdminDAO academicAdminDAO;

    @Override
    @Async
    public void logActivity(String token, ActionType actionType) {
        try {
            TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
            String userUuid = tokenInfo.getUserUuid();
            UserType userType = tokenInfo.getUserType();
            String userName = getUserName(userUuid, userType);

            logActivity(userUuid, userName, actionType);
        } catch (Exception e) {
            log.error("记录活动日志失败: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void logActivity(String userUuid, String userName, ActionType actionType) {
        try {
            boolean success = activityLogDAO.logActivity(userUuid, userName, actionType);
            if (success) {
                log.debug("活动日志记录成功 - 用户: {}, 操作: {}", userName, actionType.getActionText());
            } else {
                log.warn("活动日志记录失败 - 用户: {}, 操作: {}", userName, actionType.getActionText());
            }
        } catch (Exception e) {
            log.error("记录活动日志异常: {}", e.getMessage());
        }
    }

    /**
     * 获取用户名称
     */
    private String getUserName(String userUuid, UserType userType) {
        switch (userType) {
            case STUDENT:
                StudentDO student = studentDAO.getById(userUuid);
                return student != null ? student.getStudentName() : "未知学生";
            case TEACHER:
                TeacherDO teacher = teacherDAO.getById(userUuid);
                return teacher != null ? teacher.getTeacherName() : "未知教师";
            case ACADEMIC_ADMIN:
                AcademicAdminDO academicAdmin = academicAdminDAO.getById(userUuid);
                return academicAdmin != null ? academicAdmin.getAcademicName() : "未知教务";
            case SYSTEM_ADMIN:
                return "系统管理员";
            default:
                return "未知用户";
        }
    }
}
