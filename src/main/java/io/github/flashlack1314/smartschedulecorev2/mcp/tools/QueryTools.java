package io.github.flashlack1314.smartschedulecorev2.mcp.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP 查询工具类
 * 提供排课相关的查询功能，供 Dify AI 通过 MCP 协议调用
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryTools {

    private final ScheduleConflictDAO scheduleConflictDAO;
    private final TeacherDAO teacherDAO;
    private final ScheduleDAO scheduleDAO;
    private final CourseDAO courseDAO;
    private final ClassroomDAO classroomDAO;

    /**
     * 查询排课冲突记录
     *
     * @param semesterUuid 学期UUID（可选）
     * @param limit        返回条数限制（默认10）
     * @return 格式化的冲突记录信息
     */
    @Tool(description = "查询排课冲突记录。返回最近的冲突列表，包含冲突类型、严重程度和描述等信息。" +
            "可用于了解当前排课存在的问题。")
    public String queryConflicts(
            @ToolParam(description = "学期UUID，可选。不填则查询所有学期", required = false) String semesterUuid,
            @ToolParam(description = "限制返回条数，默认10条，最大50条", required = false) Integer limit) {

        int queryLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);

        LambdaQueryWrapper<ScheduleConflictDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(ScheduleConflictDO::getSemesterUuid, semesterUuid);
        }
        queryWrapper.orderByDesc(ScheduleConflictDO::getSeverity);
        queryWrapper.last("LIMIT " + queryLimit);

        List<ScheduleConflictDO> conflicts = scheduleConflictDAO.list(queryWrapper);

        if (conflicts.isEmpty()) {
            return "【查询结果】当前没有排课冲突记录。";
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("【查询结果】共发现 %d 条冲突记录：\n\n", conflicts.size()));

        for (int i = 0; i < conflicts.size(); i++) {
            ScheduleConflictDO conflict = conflicts.get(i);
            String severityText = conflict.getSeverity() == 1 ? "🔴 硬冲突" : "🟡 软冲突";
            result.append(String.format("%d. %s - %s\n", i + 1, severityText, conflict.getConflictType()));
            result.append(String.format("   描述：%s\n", conflict.getDescription()));
            result.append(String.format("   排课A：%s | 排课B：%s\n\n",
                    conflict.getScheduleUuidA(), conflict.getScheduleUuidB()));
        }

        return result.toString();
    }

    /**
     * 根据教师姓名查询课表
     *
     * @param teacherName  教师姓名（支持模糊匹配）
     * @param semesterUuid 学期UUID（可选）
     * @return 格式化的教师课表信息
     */
    @Tool(description = "查询教师课表。根据教师姓名查询该教师的所有排课安排，" +
            "包含课程名称、上课时间、地点等信息。支持教师姓名模糊匹配。")
    public String queryTeacherSchedule(
            @ToolParam(description = "教师姓名，支持模糊匹配") String teacherName,
            @ToolParam(description = "学期UUID，可选。不填则查询所有学期", required = false) String semesterUuid) {

        if (!StringUtils.hasText(teacherName)) {
            return "【错误】请提供教师姓名。";
        }

        // 1. 查找教师
        LambdaQueryWrapper<TeacherDO> teacherQuery = new LambdaQueryWrapper<>();
        teacherQuery.like(TeacherDO::getTeacherName, teacherName);
        List<TeacherDO> teachers = teacherDAO.list(teacherQuery);

        if (teachers.isEmpty()) {
            return String.format("【查询结果】未找到姓名包含 \"%s\" 的教师。", teacherName);
        }

        StringBuilder result = new StringBuilder();

        for (TeacherDO teacher : teachers) {
            result.append(String.format("【教师】%s（工号：%s）\n", teacher.getTeacherName(), teacher.getTeacherNum()));

            // 2. 查询该教师的排课记录
            LambdaQueryWrapper<ScheduleDO> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(ScheduleDO::getTeacherUuid, teacher.getTeacherUuid());
            if (StringUtils.hasText(semesterUuid)) {
                scheduleQuery.eq(ScheduleDO::getSemesterUuid, semesterUuid);
            }
            scheduleQuery.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
            List<ScheduleDO> schedules = scheduleDAO.list(scheduleQuery);

            if (schedules.isEmpty()) {
                result.append("  该教师暂无排课记录。\n\n");
                continue;
            }

            result.append(String.format("  排课数量：%d 条\n", schedules.size()));

            // 3. 获取课程和教室信息
            for (ScheduleDO schedule : schedules) {
                String courseName = getCourseName(schedule.getCourseUuid());
                String classroomName = getClassroomName(schedule.getClassroomUuid());
                String dayOfWeekStr = getDayOfWeekStr(schedule.getDayOfWeek());

                result.append(String.format("  - %s | %s 第%d-%d节 | %s | 周次：%s\n",
                        dayOfWeekStr,
                        courseName,
                        schedule.getSectionStart(),
                        schedule.getSectionEnd(),
                        classroomName,
                        schedule.getWeeksJson()));
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * 根据教室名称查询占用情况
     *
     * @param classroomName 教室名称（支持模糊匹配）
     * @param semesterUuid  学期UUID（可选）
     * @return 格式化的教室占用信息
     */
    @Tool(description = "查询教室占用情况。根据教室名称查询该教室的所有排课安排，" +
            "可用于了解教室使用情况或查找空闲时间。")
    public String queryClassroomOccupancy(
            @ToolParam(description = "教室名称，支持模糊匹配") String classroomName,
            @ToolParam(description = "学期UUID，可选", required = false) String semesterUuid) {

        if (!StringUtils.hasText(classroomName)) {
            return "【错误】请提供教室名称。";
        }

        // 1. 查找教室
        LambdaQueryWrapper<ClassroomDO> classroomQuery = new LambdaQueryWrapper<>();
        classroomQuery.like(ClassroomDO::getClassroomName, classroomName);
        List<ClassroomDO> classrooms = classroomDAO.list(classroomQuery);

        if (classrooms.isEmpty()) {
            return String.format("【查询结果】未找到名称包含 \"%s\" 的教室。", classroomName);
        }

        StringBuilder result = new StringBuilder();

        for (ClassroomDO classroom : classrooms) {
            result.append(String.format("【教室】%s（容量：%d人）\n",
                    classroom.getClassroomName(),
                    classroom.getClassroomCapacity() != null ? classroom.getClassroomCapacity() : 0));

            // 2. 查询该教室的排课记录
            LambdaQueryWrapper<ScheduleDO> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(ScheduleDO::getClassroomUuid, classroom.getClassroomUuid());
            if (StringUtils.hasText(semesterUuid)) {
                scheduleQuery.eq(ScheduleDO::getSemesterUuid, semesterUuid);
            }
            scheduleQuery.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
            List<ScheduleDO> schedules = scheduleDAO.list(scheduleQuery);

            if (schedules.isEmpty()) {
                result.append("  该教室暂无排课记录（空闲）。\n\n");
                continue;
            }

            result.append(String.format("  已排课数量：%d 条\n", schedules.size()));

            for (ScheduleDO schedule : schedules) {
                String teacherName = getTeacherName(schedule.getTeacherUuid());
                String courseName = getCourseName(schedule.getCourseUuid());
                String dayOfWeekStr = getDayOfWeekStr(schedule.getDayOfWeek());

                result.append(String.format("  - %s | 第%d-%d节 | %s | %s老师\n",
                        dayOfWeekStr,
                        schedule.getSectionStart(),
                        schedule.getSectionEnd(),
                        courseName,
                        teacherName));
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * 获取课程名称
     */
    private String getCourseName(String courseUuid) {
        if (!StringUtils.hasText(courseUuid)) {
            return "未知课程";
        }
        CourseDO course = courseDAO.getById(courseUuid);
        return course != null ? course.getCourseName() : "未知课程";
    }

    /**
     * 获取教师姓名
     */
    private String getTeacherName(String teacherUuid) {
        if (!StringUtils.hasText(teacherUuid)) {
            return "未知";
        }
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        return teacher != null ? teacher.getTeacherName() : "未知";
    }

    /**
     * 获取教室名称
     */
    private String getClassroomName(String classroomUuid) {
        if (!StringUtils.hasText(classroomUuid)) {
            return "未知教室";
        }
        ClassroomDO classroom = classroomDAO.getById(classroomUuid);
        return classroom != null ? classroom.getClassroomName() : "未知教室";
    }

    /**
     * 将数字转换为星期几中文
     */
    private String getDayOfWeekStr(Integer dayOfWeek) {
        if (dayOfWeek == null) {
            return "未知";
        }
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "未知";
    }
}
