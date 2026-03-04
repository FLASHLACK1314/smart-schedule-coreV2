package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ScheduleDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeachingClassDAO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 排课初始化器
 * 负责初始化排课记录
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleInitializer {

    private final ScheduleDAO scheduleDAO;
    private final TeachingClassDAO teachingClassDAO;

    /**
     * 初始化排课记录数据
     * 扩充到50条排课记录
     */
    public void initializeSchedules(
            List<TeachingClassDO> teachingClasses,
            List<SemesterDO> semesters,
            List<ClassroomDO> classrooms,
            List<CourseDO> courses,
            List<TeacherDO> teachers) {
        log.info("正在初始化排课记录数据...");

        List<ScheduleDO> schedules = new ArrayList<>();

        // 生成周次JSON字符串
        String fullWeeks = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]";
        String weeks1to8 = "[1,2,3,4,5,6,7,8]";
        String weeks1to16 = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]";
        String weeks9to16 = "[9,10,11,12,13,14,15,16]";

        // ObjectMapper 用于解析周次JSON数组
        ObjectMapper objectMapper = new ObjectMapper();

        // 辅助方法：创建排课记录
        int[][] scheduleTemplates = {
                // 周日, 节次开始, 节次结束, 教室索引, 周次类型(0-full,1-1to8,2-1to16,3-9to16)
                {1, 1, 2, 0, 1}, {1, 3, 4, 1, 0}, {2, 1, 2, 2, 0}, {2, 3, 4, 3, 2},
                {3, 1, 2, 4, 0}, {3, 3, 4, 5, 1}, {3, 5, 6, 6, 0}, {4, 1, 2, 7, 0},
                {4, 3, 4, 8, 2}, {4, 5, 6, 9, 0}, {5, 1, 2, 10, 0}, {5, 3, 4, 11, 1},
                {1, 5, 6, 12, 0}, {2, 5, 6, 13, 2}, {3, 7, 8, 14, 0}, {4, 7, 8, 15, 0},
                {5, 5, 6, 16, 0}, {1, 7, 8, 17, 2}, {2, 7, 8, 18, 0}, {3, 1, 2, 19, 1},
                {4, 1, 2, 20, 0}, {5, 7, 8, 21, 0}, {1, 1, 2, 22, 0}, {2, 3, 4, 23, 2},
                {3, 5, 6, 24, 0}, {4, 3, 4, 25, 1}, {5, 1, 2, 26, 0}, {1, 3, 4, 27, 0},
                {2, 1, 2, 28, 2}, {3, 3, 4, 29, 0}, {4, 5, 6, 30, 0}, {5, 3, 4, 31, 1},
                {1, 5, 6, 32, 0}, {2, 5, 6, 33, 0}, {3, 7, 8, 34, 2}, {4, 7, 8, 35, 0},
                {5, 5, 6, 36, 0}, {1, 7, 8, 37, 1}, {2, 7, 8, 38, 0}, {3, 1, 2, 39, 0},
                {4, 1, 2, 40, 2}, {5, 3, 4, 41, 0}, {1, 3, 4, 42, 0}, {2, 5, 6, 43, 1},
                {3, 5, 6, 44, 0}, {4, 3, 4, 45, 0}, {5, 1, 2, 46, 2}, {1, 1, 2, 47, 0},
                {2, 3, 4, 48, 0}, {3, 7, 8, 49, 1}
        };

        String[] weeksArray = {fullWeeks, weeks1to8, weeks1to16, weeks9to16};

        // 为前30个教学班生成排课记录（每个教学班1-2条）
        for (int tcIndex = 0; tcIndex < Math.min(30, teachingClasses.size()); tcIndex++) {
            TeachingClassDO tc = teachingClasses.get(tcIndex);

            // 每个教学班生成1-2条排课记录
            int recordCount = (tcIndex % 2 == 0) ? 2 : 1;

            for (int r = 0; r < recordCount; r++) {
                int templateIndex = (tcIndex * 2 + r) % scheduleTemplates.length;
                int[] template = scheduleTemplates[templateIndex];

                // 确保教室索引不越界
                int classroomIndex = template[3] % classrooms.size();

                ScheduleDO sched = new ScheduleDO();
                sched.setScheduleUuid(UuidUtil.generateUuidNoDash())
                        .setSemesterUuid(tc.getSemesterUuid())
                        .setTeachingClassUuid(tc.getTeachingClassUuid())
                        .setCourseUuid(tc.getCourseUuid())
                        .setTeacherUuid(tc.getTeacherUuid())
                        .setClassroomUuid(classrooms.get(classroomIndex).getClassroomUuid())
                        .setDayOfWeek(template[0])
                        .setSectionStart(template[1])
                        .setSectionEnd(template[2])
                        .setWeeksJson(weeksArray[template[4]])
                        .setStatus(1);

                // 计算学时
                try {
                    JsonNode weeksNode = objectMapper.readTree(weeksArray[template[4]]);
                    int weekCount = weeksNode.size();
                    int creditHours = (sched.getSectionEnd() - sched.getSectionStart() + 1) * weekCount;
                    sched.setCreditHours(creditHours);
                } catch (Exception e) {
                    log.warn("解析周次JSON失败，使用默认值0", e);
                    sched.setCreditHours(0);
                }

                schedules.add(sched);
            }
        }

        // 为剩余的教学班（如果有）添加额外的排课记录以达到50条
        int currentIndex = schedules.size();
        for (int i = currentIndex; i < 50; i++) {
            int tcIndex = i % teachingClasses.size();
            TeachingClassDO tc = teachingClasses.get(tcIndex);
            int templateIndex = i % scheduleTemplates.length;
            int[] template = scheduleTemplates[templateIndex];
            int classroomIndex = (template[3] + i) % classrooms.size();

            ScheduleDO sched = new ScheduleDO();
            sched.setScheduleUuid(UuidUtil.generateUuidNoDash())
                    .setSemesterUuid(tc.getSemesterUuid())
                    .setTeachingClassUuid(tc.getTeachingClassUuid())
                    .setCourseUuid(tc.getCourseUuid())
                    .setTeacherUuid(tc.getTeacherUuid())
                    .setClassroomUuid(classrooms.get(classroomIndex).getClassroomUuid())
                    .setDayOfWeek(template[0])
                    .setSectionStart(template[1])
                    .setSectionEnd(template[2])
                    .setWeeksJson(weeksArray[template[4]])
                    .setStatus(1);

            // 计算学时
            try {
                JsonNode weeksNode = objectMapper.readTree(weeksArray[template[4]]);
                int weekCount = weeksNode.size();
                int creditHours = (sched.getSectionEnd() - sched.getSectionStart() + 1) * weekCount;
                sched.setCreditHours(creditHours);
            } catch (Exception e) {
                log.warn("解析周次JSON失败，使用默认值0", e);
                sched.setCreditHours(0);
            }

            schedules.add(sched);
        }

        // 保存排课记录
        scheduleDAO.saveBatch(schedules);
        log.info("排课记录数据初始化完成，共 {} 条记录", schedules.size());

        // 更新所有教学班的学时统计
        log.info("正在更新教学班学时统计...");
        for (TeachingClassDO tc : teachingClasses) {
            // 统计该教学班的所有排课学时
            int totalHours = schedules.stream()
                    .filter(s -> s.getTeachingClassUuid().equals(tc.getTeachingClassUuid()))
                    .mapToInt(s -> s.getCreditHours() != null ? s.getCreditHours() : 0)
                    .sum();
            tc.setTeachingClassHours(totalHours);
            log.info("教学班 {} 学时: {}", tc.getTeachingClassName(), totalHours);
        }

        // 更新教学班学时（已存在的记录）
        teachingClassDAO.updateBatchById(teachingClasses);
        log.info("教学班学时更新完成");
    }
}
