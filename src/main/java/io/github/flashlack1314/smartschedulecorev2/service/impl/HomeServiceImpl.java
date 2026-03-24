package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.TokenInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.home.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.service.HomeService;
import io.github.flashlack1314.smartschedulecorev2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final TokenService tokenService;
    private final ActivityLogDAO activityLogDAO;
    private final AnnouncementDAO announcementDAO;
    private final StatsSnapshotDAO statsSnapshotDAO;
    private final ScheduleDAO scheduleDAO;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final ClassroomDAO classroomDAO;
    private final CourseDAO courseDAO;
    private final ClassDAO classDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;

    @Override
    public DashboardDTO getDashboard(String token) {
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();
        UserType userType = tokenInfo.getUserType();

        DashboardDTO dashboard = new DashboardDTO();

        // 用户信息
        DashboardDTO.UserInfoDTO userInfo = new DashboardDTO.UserInfoDTO();
        userInfo.setUserType(userType);
        userInfo.setName(this.getUserName(userUuid, userType));
        dashboard.setUserInfo(userInfo);

        // 统计数据
        dashboard.setStats(this.calculateStats());

        // 最近活动（学生不可见）
        if (userType != UserType.STUDENT) {
            dashboard.setRecentActivities(this.getActivities(5));
        } else {
            dashboard.setRecentActivities(Collections.emptyList());
        }

        // 今日课程
        dashboard.setTodayCourses(this.getTodayCourses(token));

        // 系统公告
        dashboard.setAnnouncements(this.getAnnouncements(token, 5));

        return dashboard;
    }

    @Override
    public List<ActivityDTO> getActivities(int limit) {
        List<ActivityLogDO> activities = activityLogDAO.getRecentActivities(limit);
        return activities.stream()
                .map(this::convertToActivityDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TodayCourseDTO> getTodayCourses(String token) {
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        String userUuid = tokenInfo.getUserUuid();
        UserType userType = tokenInfo.getUserType();

        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        switch (userType) {
            case STUDENT:
                return this.getStudentTodayCourses(userUuid, dayOfWeek);
            case TEACHER:
                return this.getTeacherTodayCourses(userUuid, dayOfWeek);
            default:
                // 管理员返回空列表
                return Collections.emptyList();
        }
    }

    @Override
    public List<AnnouncementDTO> getAnnouncements(String token, int limit) {
        TokenInfoDTO tokenInfo = tokenService.getTokenInfo(token);
        UserType userType = tokenInfo.getUserType();

        List<AnnouncementDO> announcements = announcementDAO.getAnnouncementsByUserType(userType.name(), limit);
        return announcements.stream()
                .map(this::convertToAnnouncementDTO)
                .collect(Collectors.toList());
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

    /**
     * 计算统计数据
     */
    private StatsDTO calculateStats() {
        StatsDTO stats = new StatsDTO();
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        // 获取上周快照
        StatsSnapshotDO lastWeekSnapshot = statsSnapshotDAO.getByWeekStartDate(lastWeekStart);

        // 本周排课数
        long thisWeekScheduleCount = this.countWeeklySchedules(thisWeekStart);
        long lastWeekScheduleCount = lastWeekSnapshot != null ? lastWeekSnapshot.getWeeklyScheduleCount() : 0;
        stats.setWeeklySchedule(this.createStatItem(thisWeekScheduleCount, lastWeekScheduleCount));

        // 活跃教师数
        long thisWeekActiveTeachers = this.countActiveTeachers(thisWeekStart);
        long lastWeekActiveTeachers = lastWeekSnapshot != null ? lastWeekSnapshot.getActiveTeacherCount() : 0;
        stats.setActiveTeachers(this.createStatItem(thisWeekActiveTeachers, lastWeekActiveTeachers));

        // 学生总数
        long totalStudents = studentDAO.count();
        long lastWeekStudents = lastWeekSnapshot != null ? lastWeekSnapshot.getTotalStudentCount() : 0;
        stats.setTotalStudents(this.createStatItem(totalStudents, lastWeekStudents));

        // 教室使用率
        BigDecimal thisWeekUsageRate = this.calculateClassroomUsageRate(thisWeekStart);
        BigDecimal lastWeekUsageRate = lastWeekSnapshot != null ? lastWeekSnapshot.getClassroomUsageRate() : BigDecimal.ZERO;
        stats.setClassroomUsage(this.createStatItem(thisWeekUsageRate, lastWeekUsageRate));

        return stats;
    }

    /**
     * 统计本周排课数量
     */
    private long countWeeklySchedules(LocalDate weekStart) {
        // 简化统计：统计所有状态为1（正式执行）的排课记录
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getStatus, 1);
        return scheduleDAO.count(queryWrapper);
    }

    /**
     * 统计本周活跃教师
     */
    private long countActiveTeachers(LocalDate weekStart) {
        // 简化统计：统计有排课记录的教师数量
        LambdaQueryWrapper<ScheduleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleDO::getStatus, 1);
        queryWrapper.select(ScheduleDO::getTeacherUuid);
        queryWrapper.groupBy(ScheduleDO::getTeacherUuid);
        return scheduleDAO.list(queryWrapper).size();
    }

    /**
     * 计算教室使用率
     */
    private BigDecimal calculateClassroomUsageRate(LocalDate weekStart) {
        // 获取可用教室数量
        long classroomCount = classroomDAO.count();
        if (classroomCount == 0) {
            return BigDecimal.ZERO;
        }

        // 假设每天10节课，5个工作日
        int sectionsPerDay = 10;
        int workDays = 5;
        long totalSlots = classroomCount * sectionsPerDay * workDays;

        // 统计已排课节次
        long usedSlots = scheduleDAO.count();

        if (totalSlots == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(usedSlots)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSlots), 2, RoundingMode.HALF_UP);
    }

    /**
     * 创建统计项
     */
    private StatItemDTO createStatItem(long currentValue, long lastValue) {
        StatItemDTO item = new StatItemDTO();
        item.setValue(currentValue);
        if (lastValue > 0) {
            double changeRate = (double) (currentValue - lastValue) / lastValue * 100;
            item.setChangeRate(Math.round(changeRate * 10.0) / 10.0);
        } else {
            item.setChangeRate(0.0);
        }
        return item;
    }

    /**
     * 创建统计项（BigDecimal版本）
     */
    private StatItemDTO createStatItem(BigDecimal currentValue, BigDecimal lastValue) {
        StatItemDTO item = new StatItemDTO();
        item.setValue(currentValue);
        if (lastValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changeRate = currentValue.subtract(lastValue)
                    .divide(lastValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            item.setChangeRate(changeRate.setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            item.setChangeRate(0.0);
        }
        return item;
    }

    /**
     * 获取学生今日课程
     */
    private List<TodayCourseDTO> getStudentTodayCourses(String studentUuid, int dayOfWeek) {
        // 获取学生所在班级
        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            return Collections.emptyList();
        }
        String classUuid = student.getClassUuid();

        // 查找包含该班级的教学班
        LambdaQueryWrapper<TeachingClassClassDO> tccQuery = new LambdaQueryWrapper<>();
        tccQuery.eq(TeachingClassClassDO::getClassUuid, classUuid);
        List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(tccQuery);

        if (tccList.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> teachingClassUuids = tccList.stream()
                .map(TeachingClassClassDO::getTeachingClassUuid)
                .collect(Collectors.toList());

        // 查询今日排课
        LambdaQueryWrapper<ScheduleDO> scheduleQuery = new LambdaQueryWrapper<>();
        scheduleQuery.eq(ScheduleDO::getDayOfWeek, dayOfWeek)
                .eq(ScheduleDO::getStatus, 1)
                .in(ScheduleDO::getTeachingClassUuid, teachingClassUuids)
                .orderByAsc(ScheduleDO::getSectionStart);

        List<ScheduleDO> schedules = scheduleDAO.list(scheduleQuery);
        return this.convertToTodayCourseDTOs(schedules, classUuid);
    }

    /**
     * 获取教师今日课程
     */
    private List<TodayCourseDTO> getTeacherTodayCourses(String teacherUuid, int dayOfWeek) {
        LambdaQueryWrapper<ScheduleDO> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleDO::getTeacherUuid, teacherUuid)
                .eq(ScheduleDO::getDayOfWeek, dayOfWeek)
                .eq(ScheduleDO::getStatus, 1)
                .orderByAsc(ScheduleDO::getSectionStart);

        List<ScheduleDO> schedules = scheduleDAO.list(query);
        return this.convertToTodayCourseDTOs(schedules, null);
    }

    /**
     * 转换排课记录为今日课程DTO
     */
    private List<TodayCourseDTO> convertToTodayCourseDTOs(List<ScheduleDO> schedules, String filterClassUuid) {
        List<TodayCourseDTO> result = new ArrayList<>();

        for (ScheduleDO schedule : schedules) {
            TodayCourseDTO dto = new TodayCourseDTO();
            dto.setId(schedule.getScheduleUuid());
            dto.setStartSection(schedule.getSectionStart());
            dto.setEndSection(schedule.getSectionEnd());

            // 获取课程名称
            CourseDO course = courseDAO.getById(schedule.getCourseUuid());
            dto.setCourseName(course != null ? course.getCourseName() : "未知课程");

            // 获取教室名称
            ClassroomDO classroom = classroomDAO.getById(schedule.getClassroomUuid());
            dto.setClassroomName(classroom != null ? classroom.getClassroomName() : "未知教室");

            // 获取班级名称
            if (filterClassUuid != null) {
                ClassDO classDO = classDAO.getById(filterClassUuid);
                dto.setClassName(classDO != null ? classDO.getClassName() : "未知班级");
            } else {
                // 教师视角：获取教学班关联的所有班级
                dto.setClassName(this.getClassNameByTeachingClass(schedule.getTeachingClassUuid()));
            }

            // 计算时间（假设第1节从8:00开始，每节50分钟，课间10分钟）
            dto.setStartTime(this.calculateStartTime(schedule.getSectionStart()));
            dto.setEndTime(this.calculateEndTime(schedule.getSectionEnd()));

            result.add(dto);
        }

        return result;
    }

    /**
     * 获取教学班关联的班级名称
     */
    private String getClassNameByTeachingClass(String teachingClassUuid) {
        LambdaQueryWrapper<TeachingClassClassDO> query = new LambdaQueryWrapper<>();
        query.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        List<TeachingClassClassDO> tccList = teachingClassClassDAO.list(query);

        return tccList.stream()
                .map(tcc -> {
                    ClassDO classDO = classDAO.getById(tcc.getClassUuid());
                    return classDO != null ? classDO.getClassName() : "";
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * 计算开始时间
     */
    private String calculateStartTime(Integer section) {
        // 第1节 8:00, 第2节 8:55, 第3节 10:00, 第4节 10:55, 第5节 14:00, 第6节 14:55, 第7节 16:00, 第8节 16:55, 第9节 19:00, 第10节 19:55
        int[] startMinutes = {480, 535, 600, 655, 840, 895, 960, 1015, 1140, 1195};
        if (section < 1 || section > 10) {
            return "08:00";
        }
        int minutes = startMinutes[section - 1];
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /**
     * 计算结束时间
     */
    private String calculateEndTime(Integer section) {
        int[] endMinutes = {525, 580, 645, 700, 885, 940, 1005, 1060, 1185, 1240};
        if (section < 1 || section > 10) {
            return "08:45";
        }
        int minutes = endMinutes[section - 1];
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /**
     * 转换活动记录为DTO
     */
    private ActivityDTO convertToActivityDTO(ActivityLogDO activityLog) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activityLog.getActivityUuid());
        dto.setUserName(activityLog.getUserName());
        dto.setActionType(activityLog.getActionType());
        dto.setActionText(activityLog.getActionText());
        dto.setCreatedAt(activityLog.getCreatedAt());
        return dto;
    }

    /**
     * 转换公告为DTO
     */
    private AnnouncementDTO convertToAnnouncementDTO(AnnouncementDO announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getAnnouncementUuid());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setPriority(announcement.getPriority());
        dto.setCreatedAt(announcement.getCreatedAt());
        dto.setRelativeTime(this.calculateRelativeTime(announcement.getCreatedAt()));
        return dto;
    }

    /**
     * 计算相对时间
     */
    private String calculateRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "未知时间";
        }

        long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) {
            return (minutes / 60) + "小时前";
        } else if (minutes < 10080) {
            return (minutes / 1440) + "天前";
        } else if (minutes < 43200) {
            return (minutes / 10080) + "周前";
        } else {
            return (minutes / 43200) + "个月前";
        }
    }
}
