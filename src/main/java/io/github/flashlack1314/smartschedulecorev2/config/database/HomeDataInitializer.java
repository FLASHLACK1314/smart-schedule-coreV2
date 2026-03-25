package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.enums.ActionType;
import io.github.flashlack1314.smartschedulecorev2.enums.Priority;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页数据初始化器
 * 负责在FULL模式下初始化公告、活动日志、统计快照等首页相关数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeDataInitializer {

    private final AnnouncementDAO announcementDAO;
    private final ActivityLogDAO activityLogDAO;
    private final StatsSnapshotDAO statsSnapshotDAO;
    private final TeacherDAO teacherDAO;
    private final StudentDAO studentDAO;
    private final ClassroomDAO classroomDAO;
    private final ScheduleDAO scheduleDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final SystemAdminDAO systemAdminDAO;

    /**
     * 初始化首页相关数据
     */
    public void initializeHomeData() {
        log.info("正在初始化首页相关数据...");

        // 初始化公告数据
        initializeAnnouncements();

        // 初始化活动日志
        initializeActivityLogs();

        // 初始化统计快照
        initializeStatsSnapshots();

        log.info("首页数据初始化完成");
    }

    /**
     * 初始化公告数据
     * 为不同用户类型生成公告
     */
    private void initializeAnnouncements() {
        log.info("正在初始化系统公告...");

        List<AnnouncementDO> announcements = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 面向所有用户的公告
        announcements.add(createAnnouncement(
                "欢迎使用智能排课系统",
                "智能排课系统V2已正式上线，新增遗传算法自动排课、冲突检测等功能，欢迎各位师生使用。如有问题请联系教务处。",
                Priority.HIGH,
                "STUDENT",
                now.minusDays(7)
        ));

        announcements.add(createAnnouncement(
                "系统使用指南",
                "请各位用户仔细阅读系统使用手册，了解各功能模块的操作方法。如有疑问可在工作时间咨询教务管理员。",
                Priority.MEDIUM,
                "TEACHER",
                now.minusDays(5)
        ));

        // 面向学生的公告
        announcements.add(createAnnouncement(
                "2024-2025学年第二学期选课通知",
                "各位同学请注意，第二学期选课将于下周一正式开始，请提前查看课程安排并做好选课准备。选课时间为每日8:00-22:00。",
                Priority.HIGH,
                "STUDENT",
                now.minusDays(3)
        ));

        announcements.add(createAnnouncement(
                "期中考试安排预告",
                "期中考试将于第9-10周进行，具体考试时间和地点将在第8周公布，请各位同学注意查看。",
                Priority.MEDIUM,
                "STUDENT",
                now.minusDays(2)
        ));

        // 面向教师的公告
        announcements.add(createAnnouncement(
                "教学任务确认通知",
                "请各位任课教师在系统内确认本学期教学任务，如有调整需求请在开学前两周内提交申请。",
                Priority.HIGH,
                "TEACHER",
                now.minusDays(4)
        ));

        announcements.add(createAnnouncement(
                "教室预约系统升级通知",
                "教室预约系统已完成升级，新增了智能推荐功能，可根据课程类型自动推荐合适的教室。",
                Priority.LOW,
                "TEACHER",
                now.minusDays(1)
        ));

        // 面向教务管理员的公告
        announcements.add(createAnnouncement(
                "排课冲突处理提醒",
                "系统已检测到部分排课冲突，请各位教务管理员及时登录系统查看并处理相关冲突记录。",
                Priority.HIGH,
                "ACADEMIC_ADMIN",
                now.minusDays(1)
        ));

        announcements.add(createAnnouncement(
                "教师工作量统计报告",
                "本学期教师工作量统计报告已生成，请各位管理员登录系统查看详情。",
                Priority.MEDIUM,
                "ACADEMIC_ADMIN",
                now
        ));

        // 面向系统管理员的公告
        announcements.add(createAnnouncement(
                "系统安全更新通知",
                "系统已进行安全补丁更新，请系统管理员关注服务器运行状态，确保系统稳定运行。",
                Priority.HIGH,
                "SYSTEM_ADMIN",
                now.minusDays(6)
        ));

        announcements.add(createAnnouncement(
                "数据库备份提醒",
                "请定期检查数据库自动备份任务执行情况，确保数据安全。",
                Priority.MEDIUM,
                "SYSTEM_ADMIN",
                now.minusDays(2)
        ));

        announcementDAO.saveBatch(announcements);
        log.info("系统公告初始化完成，共 {} 条", announcements.size());
    }

    /**
     * 初始化活动日志
     * 模拟各类操作记录
     */
    private void initializeActivityLogs() {
        log.info("正在初始化活动日志...");

        List<ActivityLogDO> activityLogs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 获取用户数据用于生成活动日志
        List<AcademicAdminDO> academicAdmins = academicAdminDAO.list();
        List<SystemAdminDO> systemAdmins = systemAdminDAO.list();
        List<TeacherDO> teachers = teacherDAO.list();

        // 模拟系统管理员操作
        for (SystemAdminDO admin : systemAdmins) {
            activityLogs.add(createActivityLog(
                    admin.getAdminUuid(),
                    admin.getAdminUsername(),
                    ActionType.LOGIN,
                    now.minusDays(7).minusHours(2)
            ));
            activityLogs.add(createActivityLog(
                    admin.getAdminUuid(),
                    admin.getAdminUsername(),
                    ActionType.IMPORT_DATA,
                    now.minusDays(7).minusHours(1)
            ));
            activityLogs.add(createActivityLog(
                    admin.getAdminUuid(),
                    admin.getAdminUsername(),
                    ActionType.EXPORT_TIMETABLE,
                    now.minusDays(5).minusHours(3)
            ));
        }

        // 模拟教务管理员操作
        for (AcademicAdminDO academic : academicAdmins) {
            activityLogs.add(createActivityLog(
                    academic.getAcademicUuid(),
                    academic.getAcademicName(),
                    ActionType.LOGIN,
                    now.minusDays(6).minusHours(4)
            ));
            activityLogs.add(createActivityLog(
                    academic.getAcademicUuid(),
                    academic.getAcademicName(),
                    ActionType.AUTO_SCHEDULE,
                    now.minusDays(6).minusHours(3)
            ));
            activityLogs.add(createActivityLog(
                    academic.getAcademicUuid(),
                    academic.getAcademicName(),
                    ActionType.MANUAL_SCHEDULE,
                    now.minusDays(4).minusHours(2)
            ));
            activityLogs.add(createActivityLog(
                    academic.getAcademicUuid(),
                    academic.getAcademicName(),
                    ActionType.UPDATE_SCHEDULE,
                    now.minusDays(3).minusHours(1)
            ));
            activityLogs.add(createActivityLog(
                    academic.getAcademicUuid(),
                    academic.getAcademicName(),
                    ActionType.ADD_COURSE,
                    now.minusDays(2).minusHours(5)
            ));
        }

        // 模拟教师操作
        int teacherCount = Math.min(teachers.size(), 5);
        for (int i = 0; i < teacherCount; i++) {
            TeacherDO teacher = teachers.get(i);
            activityLogs.add(createActivityLog(
                    teacher.getTeacherUuid(),
                    teacher.getTeacherName(),
                    ActionType.LOGIN,
                    now.minusDays(5).minusHours(i)
            ));
            activityLogs.add(createActivityLog(
                    teacher.getTeacherUuid(),
                    teacher.getTeacherName(),
                    ActionType.UPDATE_TEACHER,
                    now.minusDays(4).minusHours(i)
            ));
        }

        // 添加一些近期的活动日志
        if (!academicAdmins.isEmpty()) {
            AcademicAdminDO admin = academicAdmins.get(0);
            activityLogs.add(createActivityLog(
                    admin.getAcademicUuid(),
                    admin.getAcademicName(),
                    ActionType.LOGIN,
                    now.minusHours(2)
            ));
            activityLogs.add(createActivityLog(
                    admin.getAcademicUuid(),
                    admin.getAcademicName(),
                    ActionType.EXPORT_TIMETABLE,
                    now.minusHours(1)
            ));
        }

        activityLogDAO.saveBatch(activityLogs);
        log.info("活动日志初始化完成，共 {} 条", activityLogs.size());
    }

    /**
     * 初始化统计快照
     * 生成近4周的统计数据
     */
    private void initializeStatsSnapshots() {
        log.info("正在初始化统计快照...");

        List<StatsSnapshotDO> snapshots = new ArrayList<>();

        // 获取当前统计数据
        long teacherCount = teacherDAO.count();
        long studentCount = studentDAO.count();
        long classroomCount = classroomDAO.count();
        long scheduleCount = scheduleDAO.count();

        // 计算教室使用率（简化计算：假设每周可用时段为教室数 * 每周课时数）
        // 假设每周5天，每天8节课，教室使用率为已排课数 / 总可用时段
        int totalSlots = (int) (classroomCount * 5 * 8);
        BigDecimal usageRate = totalSlots > 0
                ? BigDecimal.valueOf(scheduleCount * 100.0 / totalSlots).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 生成近4周的统计快照
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = today.minusWeeks(i).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            // 每周数据略有变化，模拟真实场景
            int weekOffset = 4 - i;
            StatsSnapshotDO snapshot = new StatsSnapshotDO();
            snapshot.setSnapshotUuid(UuidUtil.generateUuidNoDash())
                    .setWeekStartDate(weekStart)
                    .setWeeklyScheduleCount((int) scheduleCount + weekOffset * 2 - i * 3)
                    .setActiveTeacherCount((int) teacherCount - i)
                    .setTotalStudentCount((int) studentCount)
                    .setClassroomUsageRate(usageRate.add(BigDecimal.valueOf(i * 2.5)))
                    .setCreatedAt(weekStart.atTime(9, 0));

            snapshots.add(snapshot);
        }

        statsSnapshotDAO.saveBatch(snapshots);
        log.info("统计快照初始化完成，共 {} 条", snapshots.size());
    }

    /**
     * 创建公告实体
     */
    private AnnouncementDO createAnnouncement(String title, String content, Priority priority,
                                               String userType, LocalDateTime createdAt) {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setAnnouncementUuid(UuidUtil.generateUuidNoDash())
                .setTitle(title)
                .setContent(content)
                .setPriority(priority.name())
                .setUserType(userType)
                .setCreatedAt(createdAt);
        return announcement;
    }

    /**
     * 创建活动日志实体
     */
    private ActivityLogDO createActivityLog(String userUuid, String userName,
                                             ActionType actionType, LocalDateTime createdAt) {
        ActivityLogDO activityLog = new ActivityLogDO();
        activityLog.setActivityUuid(UuidUtil.generateUuidNoDash())
                .setUserUuid(userUuid)
                .setUserName(userName)
                .setActionType(actionType.name())
                .setActionText(actionType.getActionText())
                .setCreatedAt(createdAt);
        return activityLog;
    }
}
