package io.github.flashlack1314.smartschedulecorev2.mcp.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.mcp.dto.SchedulePreviewDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 教育排课工具：提供排课相关的功能接口，供 MCP 调用
 * 实现自然语言驱动的智能调课功能
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EduScheduleTool {

    private final ScheduleDAO scheduleDAO;
    private final TeacherDAO teacherDAO;
    private final ClassroomDAO classroomDAO;
    private final CourseDAO courseDAO;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 预览 Key 前缀
     */
    private static final String PREVIEW_KEY_PREFIX = "schedule:preview:";

    /**
     * 预览过期时间（分钟）
     */
    private static final long PREVIEW_EXPIRE_MINUTES = 30;

    // ==================== 查询工具 ====================

    /**
     * 查询教师特定时间排课
     * 支持按星期几筛选
     *
     * @param teacherName  教师姓名（支持模糊匹配）
     * @param dayOfWeek    星期几（1-7，可选）
     * @param semesterUuid 学期UUID（可选）
     * @return 格式化的教师特定时间排课信息
     */
    @Tool(description = "查询教师特定时间的排课安排。可根据教师姓名查询特定星期几的课程，" +
            "用于了解教师的时间占用情况，便于调课时选择合适的时间。")
    public String queryTeacherScheduleByTime(
            @ToolParam(description = "教师姓名，支持模糊匹配") String teacherName,
            @ToolParam(description = "星期几（1=周一，2=周二，...，7=周日），可选", required = false) Integer dayOfWeek,
            @ToolParam(description = "学期UUID，可选", required = false) String semesterUuid) {

        if (!StringUtils.hasText(teacherName)) {
            return "【错误】请提供教师姓名。";
        }

        // 将 0 或无效边界值视为未指定（兼容 Dify 参数提取器可能返回 0 的情况）
        if (dayOfWeek != null && dayOfWeek == 0) {
            dayOfWeek = null;
        }

        // 验证星期几参数
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            return "【错误】星期几参数必须在1-7之间。";
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
            // 2. 查询该教师的排课记录
            LambdaQueryWrapper<ScheduleDO> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(ScheduleDO::getTeacherUuid, teacher.getTeacherUuid());
            // 只查询正式执行的排课
            scheduleQuery.eq(ScheduleDO::getStatus, 1);

            if (dayOfWeek != null) {
                scheduleQuery.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
            }
            if (StringUtils.hasText(semesterUuid)) {
                scheduleQuery.eq(ScheduleDO::getSemesterUuid, semesterUuid);
            }
            scheduleQuery.orderByAsc(ScheduleDO::getDayOfWeek, ScheduleDO::getSectionStart);
            List<ScheduleDO> schedules = scheduleDAO.list(scheduleQuery);

            String dayFilter = dayOfWeek != null ? getDayOfWeekStr(dayOfWeek) : "全部";
            result.append(String.format("【教师】%s（工号：%s）\n", teacher.getTeacherName(), teacher.getTeacherNum()));
            result.append(String.format("  筛选时间：%s\n", dayFilter));

            if (schedules.isEmpty()) {
                result.append("  该时间范围内暂无排课记录（空闲）。\n\n");
                continue;
            }

            result.append(String.format("  已排课数量：%d 条\n", schedules.size()));

            for (ScheduleDO schedule : schedules) {
                String courseName = getCourseName(schedule.getCourseUuid());
                String classroomName = getClassroomName(schedule.getClassroomUuid());
                String dayOfWeekStr = getDayOfWeekStr(schedule.getDayOfWeek());

                result.append(String.format("  - %s | 第%d-%d节 | %s | %s | 周次：%s\n",
                        dayOfWeekStr,
                        schedule.getSectionStart(),
                        schedule.getSectionEnd(),
                        courseName,
                        classroomName,
                        schedule.getWeeksJson()));
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * 检查时间槽可用性
     * 检查指定时间槽是否存在冲突
     *
     * @param dayOfWeek     星期几（1-7）
     * @param sectionStart  起始节次
     * @param sectionEnd    结束节次
     * @param classroomName 教室名称（可选，检查教室冲突）
     * @param teacherName   教师名称（可选，检查教师冲突）
     * @param semesterUuid  学期UUID
     * @return 时间槽可用性检测结果
     */
    @Tool(description = "检查指定时间槽是否可用。可检查教室和教师在指定时间是否有冲突，" +
            "用于调课前验证目标时间是否空闲。")
    public String checkTimeSlotAvailability(
            @ToolParam(description = "星期几（1=周一，2=周二，...，7=周日）") Integer dayOfWeek,
            @ToolParam(description = "起始节次") Integer sectionStart,
            @ToolParam(description = "结束节次") Integer sectionEnd,
            @ToolParam(description = "教室名称，可选。用于检查教室在该时间是否被占用", required = false) String classroomName,
            @ToolParam(description = "教师姓名，可选。用于检查教师在该时间是否被占用", required = false) String teacherName,
            @ToolParam(description = "学期UUID") String semesterUuid) {

        // 参数验证
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            return "【错误】星期几参数必须在1-7之间。";
        }
        if (sectionStart == null || sectionEnd == null || sectionStart > sectionEnd) {
            return "【错误】节次参数无效，起始节次不能大于结束节次。";
        }
        if (!StringUtils.hasText(semesterUuid)) {
            return "【错误】请提供学期UUID。";
        }

        StringBuilder result = new StringBuilder();
        result.append("【时间槽可用性检测】\n");
        result.append(String.format("检测时间：%s 第%d-%d节\n\n", getDayOfWeekStr(dayOfWeek), sectionStart, sectionEnd));

        boolean hasAnyConflict = false;
        List<String> conflicts = new ArrayList<>();

        // 1. 检查教室冲突
        if (StringUtils.hasText(classroomName)) {
            ClassroomDO classroom = findClassroomByName(classroomName);
            if (classroom == null) {
                result.append(String.format("⚠️ 教室 \"%s\" 未找到。\n", classroomName));
            } else {
                boolean hasConflict = checkClassroomConflict(classroom.getClassroomUuid(), semesterUuid, dayOfWeek, sectionStart, sectionEnd, null);
                if (hasConflict) {
                    hasAnyConflict = true;
                    conflicts.add("教室冲突");
                    result.append(String.format("❌ 教室 %s 在该时间段已被占用。\n", classroom.getClassroomName()));
                    // 查询具体冲突的排课
                    List<ScheduleDO> conflictSchedules = findConflictSchedules(semesterUuid, dayOfWeek, sectionStart, sectionEnd, classroom.getClassroomUuid(), null);
                    for (ScheduleDO s : conflictSchedules) {
                        result.append(String.format("   冲突项：%s - %s老师\n", getCourseName(s.getCourseUuid()), getTeacherName(s.getTeacherUuid())));
                    }
                } else {
                    result.append(String.format("✅ 教室 %s 在该时间段空闲。\n", classroom.getClassroomName()));
                }
            }
        }

        // 2. 检查教师冲突
        if (StringUtils.hasText(teacherName)) {
            TeacherDO teacher = findTeacherByName(teacherName);
            if (teacher == null) {
                result.append(String.format("⚠️ 教师 \"%s\" 未找到。\n", teacherName));
            } else {
                boolean hasConflict = checkTeacherConflict(teacher.getTeacherUuid(), semesterUuid, dayOfWeek, sectionStart, sectionEnd, null);
                if (hasConflict) {
                    hasAnyConflict = true;
                    conflicts.add("教师冲突");
                    result.append(String.format("❌ 教师 %s 在该时间段已有课程安排。\n", teacher.getTeacherName()));
                    // 查询具体冲突的排课
                    List<ScheduleDO> conflictSchedules = findConflictSchedules(semesterUuid, dayOfWeek, sectionStart, sectionEnd, null, teacher.getTeacherUuid());
                    for (ScheduleDO s : conflictSchedules) {
                        result.append(String.format("   冲突项：%s - %s\n", getCourseName(s.getCourseUuid()), getClassroomName(s.getClassroomUuid())));
                    }
                } else {
                    result.append(String.format("✅ 教师 %s 在该时间段空闲。\n", teacher.getTeacherName()));
                }
            }
        }

        // 3. 汇总结果
        result.append("\n【检测结果】");
        if (hasAnyConflict) {
            result.append(String.format("该时间槽存在冲突：%s\n", String.join("、", conflicts)));
        } else {
            result.append("该时间槽可用，无冲突。\n");
        }

        return result.toString();
    }

    // ==================== 预览工具 ====================

    /**
     * 预览调课方案
     * 生成调课预览并存入 Redis
     *
     * @param scheduleUuid     原排课记录UUID
     * @param newDayOfWeek     新星期几
     * @param newSectionStart  新起始节次
     * @param newSectionEnd    新结束节次
     * @param newClassroomName 新教室名称（可选）
     * @param semesterUuid     学期UUID
     * @return 预览结果，包含预览ID和调整前后对比
     */
    @Tool(description = "预览调课方案。将原排课调整到新的时间和教室，生成预览方案存入临时区域，" +
            "返回预览ID供确认或取消。AI应先调用此方法预览，展示给用户确认后再调用确认方法。")
    public String previewScheduleChange(
            @ToolParam(description = "原排课记录UUID") String scheduleUuid,
            @ToolParam(description = "新星期几（1=周一，2=周二，...，7=周日）") Integer newDayOfWeek,
            @ToolParam(description = "新起始节次") Integer newSectionStart,
            @ToolParam(description = "新结束节次") Integer newSectionEnd,
            @ToolParam(description = "新教室名称，可选。不填则保持原教室", required = false) String newClassroomName,
            @ToolParam(description = "学期UUID") String semesterUuid) {

        // 参数验证
        if (!StringUtils.hasText(scheduleUuid)) {
            return "【错误】请提供原排课记录UUID。";
        }
        if (newDayOfWeek == null || newDayOfWeek < 1 || newDayOfWeek > 7) {
            return "【错误】新星期几参数必须在1-7之间。";
        }
        if (newSectionStart == null || newSectionEnd == null || newSectionStart > newSectionEnd) {
            return "【错误】节次参数无效，起始节次不能大于结束节次。";
        }
        if (!StringUtils.hasText(semesterUuid)) {
            return "【错误】请提供学期UUID。";
        }

        // 1. 查询原排课记录
        ScheduleDO originalSchedule = scheduleDAO.getById(scheduleUuid);
        if (originalSchedule == null) {
            return String.format("【错误】未找到排课记录：%s", scheduleUuid);
        }

        // 2. 构建原排课信息
        SchedulePreviewDTO.ScheduleInfo originalInfo = buildScheduleInfo(originalSchedule);

        // 3. 确定新教室
        String newClassroomUuid = originalSchedule.getClassroomUuid();
        String newClassroomNameActual = getClassroomName(originalSchedule.getClassroomUuid());
        if (StringUtils.hasText(newClassroomName)) {
            ClassroomDO newClassroom = findClassroomByName(newClassroomName);
            if (newClassroom == null) {
                return String.format("【错误】未找到教室：%s", newClassroomName);
            }
            newClassroomUuid = newClassroom.getClassroomUuid();
            newClassroomNameActual = newClassroom.getClassroomName();
        }

        // 4. 构建新排课信息
        SchedulePreviewDTO.ScheduleInfo newInfo = new SchedulePreviewDTO.ScheduleInfo()
                .setCourseName(originalInfo.getCourseName())
                .setTeacherName(originalInfo.getTeacherName())
                .setClassroomName(newClassroomNameActual)
                .setDayOfWeek(newDayOfWeek)
                .setDayOfWeekStr(getDayOfWeekStr(newDayOfWeek))
                .setSectionStart(newSectionStart)
                .setSectionEnd(newSectionEnd)
                .setWeeksJson(originalSchedule.getWeeksJson());

        // 5. 冲突检测
        SchedulePreviewDTO.ConflictResult conflictResult = detectConflicts(
                semesterUuid, newDayOfWeek, newSectionStart, newSectionEnd,
                newClassroomUuid, originalSchedule.getTeacherUuid(), scheduleUuid);

        // 6. 生成预览ID
        String previewId = UUID.randomUUID().toString().replace("-", "");

        // 7. 构建预览DTO
        SchedulePreviewDTO previewDTO = new SchedulePreviewDTO()
                .setPreviewId(previewId)
                .setSemesterUuid(semesterUuid)
                .setOriginalScheduleUuid(scheduleUuid)
                .setOriginalSchedule(originalInfo)
                .setNewSchedule(newInfo)
                .setConflictResult(conflictResult)
                .setCreatedAt(LocalDateTime.now());

        // 8. 存入 Redis
        String redisKey = PREVIEW_KEY_PREFIX + previewId;
        redisTemplate.opsForValue().set(redisKey, previewDTO, PREVIEW_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("调课预览已生成，previewId: {}, 原排课: {}", previewId, scheduleUuid);

        // 9. 构建返回结果
        StringBuilder result = new StringBuilder();
        result.append("【调课预览已生成】\n");
        result.append(String.format("预览ID：%s\n", previewId));
        result.append(String.format("有效期：%d 分钟\n\n", PREVIEW_EXPIRE_MINUTES));

        result.append("【调整前】\n");
        result.append(formatScheduleInfo(originalInfo));
        result.append("\n【调整后】\n");
        result.append(formatScheduleInfo(newInfo));

        result.append("\n【冲突检测】\n");
        if (conflictResult.getHasConflict()) {
            result.append(String.format("⚠️ 检测到冲突：%s\n", String.join("、", conflictResult.getConflictTypes())));
            result.append(String.format("详情：%s\n", conflictResult.getConflictDescription()));
            result.append("\n请注意：存在冲突的方案可能会导致排课问题，请谨慎确认。\n");
        } else {
            result.append("✅ 无冲突，方案可行。\n");
        }

        result.append("\n请确认后使用 confirmScheduleChange 确认，或使用 cancelPreview 取消。");

        return result.toString();
    }

    /**
     * 确认调课
     * 将预览方案持久化到数据库
     *
     * @param previewId 预览ID
     * @return 操作结果
     */
    @Tool(description = "确认调课预览方案。将预览区域的调课方案应用到正式排课，完成调课操作。")
    public String confirmScheduleChange(
            @ToolParam(description = "预览ID，由 previewScheduleChange 返回") String previewId) {

        if (!StringUtils.hasText(previewId)) {
            return "【错误】请提供预览ID。";
        }

        // 1. 从 Redis 获取预览数据
        String redisKey = PREVIEW_KEY_PREFIX + previewId;
        Object obj = redisTemplate.opsForValue().get(redisKey);

        if (obj == null) {
            return String.format("【错误】预览不存在或已过期：%s", previewId);
        }

        SchedulePreviewDTO previewDTO;
        try {
            previewDTO = (SchedulePreviewDTO) obj;
        } catch (ClassCastException e) {
            log.error("预览数据类型转换失败", e);
            return "【错误】预览数据格式异常。";
        }

        // 2. 查询原排课记录
        ScheduleDO originalSchedule = scheduleDAO.getById(previewDTO.getOriginalScheduleUuid());
        if (originalSchedule == null) {
            return String.format("【错误】原排课记录不存在：%s", previewDTO.getOriginalScheduleUuid());
        }

        // 3. 查找新教室UUID
        String newClassroomUuid = originalSchedule.getClassroomUuid();
        SchedulePreviewDTO.ScheduleInfo newInfo = previewDTO.getNewSchedule();
        if (!newInfo.getClassroomName().equals(getClassroomName(originalSchedule.getClassroomUuid()))) {
            ClassroomDO newClassroom = findClassroomByName(newInfo.getClassroomName());
            if (newClassroom != null) {
                newClassroomUuid = newClassroom.getClassroomUuid();
            }
        }

        // 4. 更新排课记录
        originalSchedule.setDayOfWeek(newInfo.getDayOfWeek());
        originalSchedule.setSectionStart(newInfo.getSectionStart());
        originalSchedule.setSectionEnd(newInfo.getSectionEnd());
        originalSchedule.setClassroomUuid(newClassroomUuid);
        originalSchedule.setUpdatedAt(LocalDateTime.now());

        boolean updated = scheduleDAO.updateById(originalSchedule);

        if (!updated) {
            return "【错误】更新排课记录失败。";
        }

        // 5. 删除预览缓存
        redisTemplate.delete(redisKey);

        log.info("调课已确认，原排课: {}, 新时间: {} 第{}-{}节",
                previewDTO.getOriginalScheduleUuid(),
                getDayOfWeekStr(newInfo.getDayOfWeek()),
                newInfo.getSectionStart(),
                newInfo.getSectionEnd());

        StringBuilder result = new StringBuilder();
        result.append("【调课成功】\n");
        result.append(String.format("排课记录：%s\n", previewDTO.getOriginalScheduleUuid()));
        result.append(String.format("课程：%s\n", newInfo.getCourseName()));
        result.append(String.format("教师：%s\n", newInfo.getTeacherName()));
        result.append(String.format("新时间：%s 第%d-%d节\n",
                newInfo.getDayOfWeekStr(), newInfo.getSectionStart(), newInfo.getSectionEnd()));
        result.append(String.format("新教室：%s\n", newInfo.getClassroomName()));

        return result.toString();
    }

    /**
     * 取消预览
     * 清理 Redis 中的预览缓存
     *
     * @param previewId 预览ID
     * @return 操作结果
     */
    @Tool(description = "取消调课预览。删除预览区域的调课方案，不做任何实际修改。")
    public String cancelPreview(
            @ToolParam(description = "预览ID，由 previewScheduleChange 返回") String previewId) {

        if (!StringUtils.hasText(previewId)) {
            return "【错误】请提供预览ID。";
        }

        String redisKey = PREVIEW_KEY_PREFIX + previewId;
        Boolean deleted = redisTemplate.delete(redisKey);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("调课预览已取消，previewId: {}", previewId);
            return String.format("【成功】预览已取消：%s", previewId);
        } else {
            return String.format("【提示】预览不存在或已过期：%s", previewId);
        }
    }

    // ==================== 智能调课工具（自然语言友好） ====================

    /**
     * 根据自然语言参数预览调课方案
     * 内部自动查找排课UUID，支持多匹配场景返回选择列表
     *
     * @param teacherName        教师姓名（模糊匹配）
     * @param originalDayOfWeek  原排课星期几（可选，1-7）
     * @param originalSectionStart 原排课起始节次（可选）
     * @param originalSectionEnd   原排课结束节次（可选）
     * @param courseNameKeyword  课程名关键词（可选）
     * @param newDayOfWeek       目标星期几（必填）
     * @param newSectionStart    目标起始节次（必填）
     * @param newSectionEnd      目标结束节次（必填）
     * @param newClassroomName   目标教室名称（可选，不填保持原教室）
     * @param semesterUuid       学期UUID（必填）
     * @return 预览结果或选择列表
     */
    @Tool(description = "根据教师姓名和时间信息预览调课方案。当用户说'把张三老师周五的课调到周四下午'时使用此工具。" +
            "系统会自动查找匹配的排课记录：如果只有一条匹配则直接返回预览；如果有多条匹配则返回选择列表供用户选择。")
    public String previewScheduleChangeByInfo(
            @ToolParam(description = "教师姓名，支持模糊匹配（如'张三'、'张教授'）") String teacherName,
            @ToolParam(description = "原排课星期几（1=周一，2=周二，...，7=周日），不填则查所有", required = false) Integer originalDayOfWeek,
            @ToolParam(description = "原排课起始节次，不填则不限制", required = false) Integer originalSectionStart,
            @ToolParam(description = "原排课结束节次，不填则不限制", required = false) Integer originalSectionEnd,
            @ToolParam(description = "课程名关键词（如'高数'、'英语'），可选", required = false) String courseNameKeyword,
            @ToolParam(description = "目标星期几（1=周一，2=周二，...，7=周日）") Integer newDayOfWeek,
            @ToolParam(description = "目标起始节次") Integer newSectionStart,
            @ToolParam(description = "目标结束节次") Integer newSectionEnd,
            @ToolParam(description = "目标教室名称，不填则保持原教室", required = false) String newClassroomName,
            @ToolParam(description = "学期UUID") String semesterUuid) {

        // 参数验证
        if (!StringUtils.hasText(teacherName)) {
            return "【错误】请提供教师姓名。";
        }
        if (newDayOfWeek == null || newDayOfWeek < 1 || newDayOfWeek > 7) {
            return "【错误】目标星期几参数必须在1-7之间。";
        }
        if (newSectionStart == null || newSectionEnd == null || newSectionStart > newSectionEnd) {
            return "【错误】目标节次参数无效，起始节次不能大于结束节次。";
        }
        if (!StringUtils.hasText(semesterUuid)) {
            return "【错误】请提供学期UUID。";
        }

        // 将 0 或无效边界值视为未指定（兼容 Dify 参数提取器可能返回 0 的情况）
        if (originalDayOfWeek != null && originalDayOfWeek <= 0) {
            originalDayOfWeek = null;
        }
        if (originalSectionStart != null && originalSectionStart <= 0) {
            originalSectionStart = null;
        }
        if (originalSectionEnd != null && originalSectionEnd <= 0) {
            originalSectionEnd = null;
        }

        // 查找匹配的排课记录
        List<ScheduleDO> matchedSchedules = findSchedulesByCondition(
                teacherName, originalDayOfWeek, originalSectionStart, originalSectionEnd,
                courseNameKeyword, semesterUuid);

        if (matchedSchedules.isEmpty()) {
            return buildNoMatchResult(teacherName, originalDayOfWeek, originalSectionStart, originalSectionEnd, courseNameKeyword);
        }

        // 唯一匹配：直接生成预览
        if (matchedSchedules.size() == 1) {
            ScheduleDO schedule = matchedSchedules.get(0);
            return previewScheduleChange(
                    schedule.getScheduleUuid(),
                    newDayOfWeek,
                    newSectionStart,
                    newSectionEnd,
                    newClassroomName,
                    semesterUuid);
        }

        // 多个匹配：返回选择列表
        return buildSelectionList(matchedSchedules, semesterUuid, newDayOfWeek, newSectionStart, newSectionEnd, newClassroomName);
    }

    /**
     * 根据选择码预览调课方案
     * 当用户从多个匹配项中选择后，使用选择码生成预览
     *
     * @param selectionCode   选择码（从列表中获取）
     * @param newDayOfWeek    目标星期几
     * @param newSectionStart 目标起始节次
     * @param newSectionEnd   目标结束节次
     * @param newClassroomName 目标教室名称（可选）
     * @param semesterUuid    学期UUID
     * @return 预览结果
     */
    @Tool(description = "根据选择码预览调课方案。当系统返回多个匹配项后，用户选择其中一个时使用。" +
            "选择码从之前返回的选择列表中获取。⚠️ 注意：此工具仅用于用户从选择列表中选取的场景，" +
            "如果用户是首次提出调课需求（如'把张教授的课调到周四'），请使用 previewScheduleChangeByInfo 工具。")
    public String previewBySelectionCode(
            @ToolParam(description = "选择码，从选择列表中获取（如'A1B2C3D4'）。如果用户没有提供选择码，请留空。") String selectionCode,
            @ToolParam(description = "选择序号，用户说'第一个/第二门'时使用。如果用户没有明确说序号，请留空。", required = false) Integer choiceIndex,
            @ToolParam(description = "目标星期几（1=周一，2=周二，...，7=周日）") Integer newDayOfWeek,
            @ToolParam(description = "目标起始节次") Integer newSectionStart,
            @ToolParam(description = "目标结束节次") Integer newSectionEnd,
            @ToolParam(description = "目标教室名称，不填则保持原教室", required = false) String newClassroomName,
            @ToolParam(description = "学期UUID") String semesterUuid) {

        // 参数验证：选择码和选择序号至少需要一个
        if (!StringUtils.hasText(selectionCode) && choiceIndex == null) {
            return "【提示】此功能用于从选择列表中选取排课记录。\n\n" +
                   "您似乎是想直接调课而不是从列表选择？\n" +
                   "请直接描述您的调课需求，例如：\n" +
                   "- \"把张教授周五的课调到周四下午第5-6节\"\n" +
                   "- \"将王老师周一第3-4节的数学课调整到周三\"\n\n" +
                   "如果您确实是想从列表中选择，请告诉我选择码或序号（如\"选择A1B2C3D4\"或\"第一门\"）。";
        }
        if (newDayOfWeek == null || newDayOfWeek < 1 || newDayOfWeek > 7) {
            return "【错误】目标星期几参数必须在1-7之间。";
        }
        if (newSectionStart == null || newSectionEnd == null || newSectionStart > newSectionEnd) {
            return "【错误】目标节次参数无效，起始节次不能大于结束节次。";
        }
        if (!StringUtils.hasText(semesterUuid)) {
            return "【错误】请提供学期UUID。";
        }

        // 从 Redis 获取排课UUID
        String scheduleUuid = null;

        if (StringUtils.hasText(selectionCode)) {
            // 通过选择码获取
            scheduleUuid = getScheduleUuidFromSelectionCode(selectionCode, semesterUuid);
            if (scheduleUuid == null) {
                return String.format("【错误】选择码无效或已过期：%s。请重新查询排课列表。", selectionCode);
            }
        } else if (choiceIndex != null && choiceIndex > 0) {
            // 通过序号获取
            scheduleUuid = getScheduleUuidByIndex(choiceIndex, semesterUuid);
            if (scheduleUuid == null) {
                return String.format("【错误】序号 %d 无效或选择列表已过期。请重新查询排课列表。", choiceIndex);
            }
        }

        // 调用原有预览方法
        return previewScheduleChange(scheduleUuid, newDayOfWeek, newSectionStart, newSectionEnd, newClassroomName, semesterUuid);
    }

    /**
     * 构建无匹配结果提示
     */
    private String buildNoMatchResult(String teacherName, Integer dayOfWeek,
                                       Integer sectionStart, Integer sectionEnd,
                                       String courseNameKeyword) {
        StringBuilder result = new StringBuilder();
        result.append("【未找到匹配的排课记录】\n");
        result.append(String.format("教师：%s\n", teacherName));

        if (dayOfWeek != null) {
            result.append(String.format("筛选星期：%s\n", getDayOfWeekStr(dayOfWeek)));
        }
        if (sectionStart != null && sectionEnd != null) {
            result.append(String.format("筛选节次：第%d-%d节\n", sectionStart, sectionEnd));
        }
        if (StringUtils.hasText(courseNameKeyword)) {
            result.append(String.format("筛选课程关键词：%s\n", courseNameKeyword));
        }

        result.append("\n请尝试：\n");
        result.append("1. 减少筛选条件（如不指定节次、课程名）\n");
        result.append("2. 确认教师姓名是否正确\n");
        result.append("3. 确认学期UUID是否正确\n");

        return result.toString();
    }

    /**
     * 构建选择列表
     */
    private String buildSelectionList(List<ScheduleDO> schedules, String semesterUuid,
                                       Integer newDayOfWeek, Integer newSectionStart,
                                       Integer newSectionEnd, String newClassroomName) {
        StringBuilder result = new StringBuilder();
        result.append("【找到多个匹配的排课记录，请选择】\n");
        result.append(String.format("匹配数量：%d 条\n\n", schedules.size()));

        // 清空之前的序号列表，重新存储
        String listKey = "schedule:selection:list:" + semesterUuid;
        redisTemplate.delete(listKey);

        int index = 1;
        for (ScheduleDO schedule : schedules) {
            String selectionCode = generateSelectionCode(schedule.getScheduleUuid());
            // 存储选择码映射
            storeSelectionCodeMapping(selectionCode, schedule.getScheduleUuid(), semesterUuid);
            // 存储到序号列表（支持"第X门"的选择方式）
            redisTemplate.opsForList().rightPush(listKey, schedule.getScheduleUuid());

            String courseName = getCourseName(schedule.getCourseUuid());
            String classroomName = getClassroomName(schedule.getClassroomUuid());
            String dayOfWeekStr = getDayOfWeekStr(schedule.getDayOfWeek());

            result.append(String.format("[%d] %s | %s 第%d-%d节 | %s | %s (选择码: %s)\n",
                    index,
                    courseName,
                    dayOfWeekStr,
                    schedule.getSectionStart(),
                    schedule.getSectionEnd(),
                    classroomName,
                    schedule.getWeeksJson() != null ? "周次：" + schedule.getWeeksJson() : "",
                    selectionCode));

            if (index++ >= 10) {
                result.append("... 还有更多记录，请增加筛选条件缩小范围\n");
                break;
            }
        }

        // 设置列表过期时间
        redisTemplate.expire(listKey, PREVIEW_EXPIRE_MINUTES, TimeUnit.MINUTES);

        result.append("\n请告诉我要调整哪一门课，例如：\"选择第1门\" 或 \"选择 A1B2C3D4\"\n");
        result.append(String.format("\n目标时间：%s 第%d-%d节",
                getDayOfWeekStr(newDayOfWeek), newSectionStart, newSectionEnd));
        if (StringUtils.hasText(newClassroomName)) {
            result.append(String.format("，教室：%s", newClassroomName));
        }

        return result.toString();
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据条件查找排课列表
     * 支持教师姓名、星期、节次、课程名关键词等多条件组合查询
     *
     * @param teacherName       教师姓名（模糊匹配）
     * @param dayOfWeek         星期几（可选）
     * @param sectionStart      起始节次（可选）
     * @param sectionEnd        结束节次（可选）
     * @param courseNameKeyword 课程名关键词（可选）
     * @param semesterUuid      学期UUID
     * @return 匹配的排课列表
     */
    private List<ScheduleDO> findSchedulesByCondition(String teacherName, Integer dayOfWeek,
                                                       Integer sectionStart, Integer sectionEnd,
                                                       String courseNameKeyword, String semesterUuid) {
        // 1. 查找教师
        if (!StringUtils.hasText(teacherName)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<TeacherDO> teacherQuery = new LambdaQueryWrapper<>();
        teacherQuery.like(TeacherDO::getTeacherName, teacherName);
        List<TeacherDO> teachers = teacherDAO.list(teacherQuery);

        if (teachers.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScheduleDO> allSchedules = new ArrayList<>();

        for (TeacherDO teacher : teachers) {
            // 2. 查询该教师的排课记录
            LambdaQueryWrapper<ScheduleDO> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(ScheduleDO::getTeacherUuid, teacher.getTeacherUuid());
            scheduleQuery.eq(ScheduleDO::getStatus, 1); // 只查询正式执行的排课

            if (dayOfWeek != null && dayOfWeek > 0) {
                scheduleQuery.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
            }
            if (StringUtils.hasText(semesterUuid)) {
                scheduleQuery.eq(ScheduleDO::getSemesterUuid, semesterUuid);
            }
            // 节次筛选：排课的节次范围与查询范围有交集
            if (sectionStart != null && sectionEnd != null) {
                scheduleQuery.le(ScheduleDO::getSectionStart, sectionEnd);
                scheduleQuery.ge(ScheduleDO::getSectionEnd, sectionStart);
            }

            List<ScheduleDO> schedules = scheduleDAO.list(scheduleQuery);
            allSchedules.addAll(schedules);
        }

        // 3. 课程名关键词筛选
        if (StringUtils.hasText(courseNameKeyword)) {
            List<ScheduleDO> filtered = new ArrayList<>();
            for (ScheduleDO schedule : allSchedules) {
                String courseName = getCourseName(schedule.getCourseUuid());
                if (courseName.contains(courseNameKeyword)) {
                    filtered.add(schedule);
                }
            }
            return filtered;
        }

        return allSchedules;
    }

    /**
     * 生成选择码
     * 将排课UUID编码为简短的选择码
     */
    private String generateSelectionCode(String scheduleUuid) {
        // 使用 Base64 编码简化显示
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(scheduleUuid.getBytes())
                .substring(0, 8).toUpperCase();
    }

    /**
     * 从选择码解析排课UUID
     * Redis 中存储选择码与UUID的映射
     */
    private String getScheduleUuidFromSelectionCode(String selectionCode, String semesterUuid) {
        String redisKey = "schedule:selection:" + semesterUuid + ":" + selectionCode;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 根据序号获取排课UUID
     * Redis 中存储序号列表的键为 "schedule:selection:list:{semesterUuid}"
     */
    private String getScheduleUuidByIndex(int index, String semesterUuid) {
        String listKey = "schedule:selection:list:" + semesterUuid;
        Object obj = redisTemplate.opsForList().index(listKey, index - 1); // 序号从1开始，列表索引从0开始
        return obj != null ? obj.toString() : null;
    }

    /**
     * 存储选择码与排课UUID的映射
     */
    private void storeSelectionCodeMapping(String selectionCode, String scheduleUuid, String semesterUuid) {
        String redisKey = "schedule:selection:" + semesterUuid + ":" + selectionCode;
        redisTemplate.opsForValue().set(redisKey, scheduleUuid, PREVIEW_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 查找教室（模糊匹配）
     */
    private ClassroomDO findClassroomByName(String classroomName) {
        LambdaQueryWrapper<ClassroomDO> query = new LambdaQueryWrapper<>();
        query.like(ClassroomDO::getClassroomName, classroomName);
        List<ClassroomDO> classrooms = classroomDAO.list(query);
        return classrooms.isEmpty() ? null : classrooms.get(0);
    }

    /**
     * 查找教师（模糊匹配）
     */
    private TeacherDO findTeacherByName(String teacherName) {
        LambdaQueryWrapper<TeacherDO> query = new LambdaQueryWrapper<>();
        query.like(TeacherDO::getTeacherName, teacherName);
        List<TeacherDO> teachers = teacherDAO.list(query);
        return teachers.isEmpty() ? null : teachers.get(0);
    }

    /**
     * 检查教室时间冲突
     */
    private boolean checkClassroomConflict(String classroomUuid, String semesterUuid,
                                           Integer dayOfWeek, Integer sectionStart, Integer sectionEnd,
                                           String excludeScheduleUuid) {
        LambdaQueryWrapper<ScheduleDO> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        query.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        query.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
        query.eq(ScheduleDO::getStatus, 1);
        query.le(ScheduleDO::getSectionStart, sectionEnd);
        query.ge(ScheduleDO::getSectionEnd, sectionStart);

        if (StringUtils.hasText(excludeScheduleUuid)) {
            query.ne(ScheduleDO::getScheduleUuid, excludeScheduleUuid);
        }

        return scheduleDAO.count(query) > 0;
    }

    /**
     * 检查教师时间冲突
     */
    private boolean checkTeacherConflict(String teacherUuid, String semesterUuid,
                                         Integer dayOfWeek, Integer sectionStart, Integer sectionEnd,
                                         String excludeScheduleUuid) {
        LambdaQueryWrapper<ScheduleDO> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleDO::getTeacherUuid, teacherUuid);
        query.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        query.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
        query.eq(ScheduleDO::getStatus, 1);
        query.le(ScheduleDO::getSectionStart, sectionEnd);
        query.ge(ScheduleDO::getSectionEnd, sectionStart);

        if (StringUtils.hasText(excludeScheduleUuid)) {
            query.ne(ScheduleDO::getScheduleUuid, excludeScheduleUuid);
        }

        return scheduleDAO.count(query) > 0;
    }

    /**
     * 查找冲突的排课记录
     */
    private List<ScheduleDO> findConflictSchedules(String semesterUuid, Integer dayOfWeek,
                                                   Integer sectionStart, Integer sectionEnd,
                                                   String classroomUuid, String teacherUuid) {
        LambdaQueryWrapper<ScheduleDO> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleDO::getSemesterUuid, semesterUuid);
        query.eq(ScheduleDO::getDayOfWeek, dayOfWeek);
        query.eq(ScheduleDO::getStatus, 1);
        query.le(ScheduleDO::getSectionStart, sectionEnd);
        query.ge(ScheduleDO::getSectionEnd, sectionStart);

        if (StringUtils.hasText(classroomUuid)) {
            query.eq(ScheduleDO::getClassroomUuid, classroomUuid);
        }
        if (StringUtils.hasText(teacherUuid)) {
            query.eq(ScheduleDO::getTeacherUuid, teacherUuid);
        }

        return scheduleDAO.list(query);
    }

    /**
     * 冲突检测综合方法
     */
    private SchedulePreviewDTO.ConflictResult detectConflicts(String semesterUuid, Integer dayOfWeek,
                                                              Integer sectionStart, Integer sectionEnd,
                                                              String classroomUuid, String teacherUuid,
                                                              String excludeScheduleUuid) {
        List<String> conflictTypes = new ArrayList<>();
        StringBuilder description = new StringBuilder();

        // 检查教室冲突
        boolean classroomConflict = checkClassroomConflict(classroomUuid, semesterUuid, dayOfWeek, sectionStart, sectionEnd, excludeScheduleUuid);
        if (classroomConflict) {
            conflictTypes.add("教室时间冲突");
            description.append("教室在该时段已有课程安排。");
        }

        // 检查教师冲突
        boolean teacherConflict = checkTeacherConflict(teacherUuid, semesterUuid, dayOfWeek, sectionStart, sectionEnd, excludeScheduleUuid);
        if (teacherConflict) {
            conflictTypes.add("教师时间冲突");
            if (!description.isEmpty()) {
                description.append(" ");
            }
            description.append("教师在该时段已有课程安排。");
        }

        return new SchedulePreviewDTO.ConflictResult()
                .setHasConflict(!conflictTypes.isEmpty())
                .setConflictTypes(conflictTypes)
                .setConflictDescription(!description.isEmpty() ? description.toString() : "无冲突");
    }

    /**
     * 构建排课信息
     */
    private SchedulePreviewDTO.ScheduleInfo buildScheduleInfo(ScheduleDO schedule) {
        return new SchedulePreviewDTO.ScheduleInfo()
                .setCourseName(getCourseName(schedule.getCourseUuid()))
                .setTeacherName(getTeacherName(schedule.getTeacherUuid()))
                .setClassroomName(getClassroomName(schedule.getClassroomUuid()))
                .setDayOfWeek(schedule.getDayOfWeek())
                .setDayOfWeekStr(getDayOfWeekStr(schedule.getDayOfWeek()))
                .setSectionStart(schedule.getSectionStart())
                .setSectionEnd(schedule.getSectionEnd())
                .setWeeksJson(schedule.getWeeksJson());
    }

    /**
     * 格式化排课信息
     */
    private String formatScheduleInfo(SchedulePreviewDTO.ScheduleInfo info) {
        return String.format("  课程：%s\n  教师：%s\n  教室：%s\n  时间：%s 第%d-%d节\n  周次：%s\n",
                info.getCourseName(),
                info.getTeacherName(),
                info.getClassroomName(),
                info.getDayOfWeekStr(),
                info.getSectionStart(),
                info.getSectionEnd(),
                info.getWeeksJson());
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