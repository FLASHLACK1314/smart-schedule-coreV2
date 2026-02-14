package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScheduleVO;
import io.github.flashlack1314.smartschedulecorev2.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排课服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleDAO scheduleDAO;
    private final SemesterDAO semesterDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final ClassroomDAO classroomDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final StudentDAO studentDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String addSchedule(AddScheduleVO getData) {
        log.info("添加排课 - 学期: {}, 教学班: {}, 教室: {}, 星期: {}, 节次: {}-{}",
                getData.getSemesterUuid(), getData.getTeachingClassUuid(),
                getData.getClassroomUuid(), getData.getDayOfWeek(),
                getData.getSectionStart(), getData.getSectionEnd());

        // 验证学期是否存在
        SemesterDO semesterDO = semesterDAO.getById(getData.getSemesterUuid());
        if (semesterDO == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教学班是否存在
        TeachingClassDO teachingClassDO = teachingClassDAO.getById(getData.getTeachingClassUuid());
        if (teachingClassDO == null) {
            throw new BusinessException("教学班不存在: " + getData.getTeachingClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教室是否存在
        ClassroomDO classroomDO = classroomDAO.getById(getData.getClassroomUuid());
        if (classroomDO == null) {
            throw new BusinessException("教室不存在: " + getData.getClassroomUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证时间参数
        if (getData.getDayOfWeek() == null || getData.getDayOfWeek() < 1 || getData.getDayOfWeek() > 7) {
            throw new BusinessException("星期几必须在1-7之间", ErrorCode.OPERATION_FAILED);
        }
        if (getData.getSectionStart() == null || getData.getSectionStart() < 1) {
            throw new BusinessException("起始节次必须大于0", ErrorCode.OPERATION_FAILED);
        }
        if (getData.getSectionEnd() == null || getData.getSectionEnd() < getData.getSectionStart()) {
            throw new BusinessException("结束节次必须大于等于起始节次", ErrorCode.OPERATION_FAILED);
        }

        // 验证weeksJson格式
        if (getData.getWeeksJson() == null || getData.getWeeksJson().trim().isEmpty()) {
            throw new BusinessException("上课周次不能为空", ErrorCode.OPERATION_FAILED);
        }
        try {
            JsonNode weeksNode = objectMapper.readTree(getData.getWeeksJson());
            // 验证周次是否超出学期范围
            int maxWeek = 0;
            for (JsonNode weekNode : weeksNode) {
                int week = weekNode.asInt();
                if (week > maxWeek) {
                    maxWeek = week;
                }
            }
            if (maxWeek > semesterDO.getSemesterWeeks()) {
                throw new BusinessException("上课周次不能超过学期周数(" + semesterDO.getSemesterWeeks() + "周)", ErrorCode.OPERATION_FAILED);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("上课周次格式错误，应为JSON数组格式", ErrorCode.OPERATION_FAILED);
        }

        // 验证状态
        if (getData.getStatus() == null || (getData.getStatus() != 0 && getData.getStatus() != 1)) {
            throw new BusinessException("状态必须为0（预览）或1（正式）", ErrorCode.OPERATION_FAILED);
        }

        // 获取课程和教师信息（填充冗余字段）
        CourseDO courseDO = courseDAO.getById(teachingClassDO.getCourseUuid());
        if (courseDO == null) {
            throw new BusinessException("课程不存在: " + teachingClassDO.getCourseUuid(), ErrorCode.OPERATION_FAILED);
        }

        TeacherDO teacherDO = teacherDAO.getById(teachingClassDO.getTeacherUuid());
        if (teacherDO == null) {
            throw new BusinessException("教师不存在: " + teachingClassDO.getTeacherUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 创建排课对象
        ScheduleDO scheduleDO = new ScheduleDO();
        scheduleDO.setScheduleUuid(UuidUtil.generateUuidNoDash());
        scheduleDO.setSemesterUuid(getData.getSemesterUuid());
        scheduleDO.setTeachingClassUuid(getData.getTeachingClassUuid());
        scheduleDO.setCourseUuid(teachingClassDO.getCourseUuid());
        scheduleDO.setTeacherUuid(teachingClassDO.getTeacherUuid());
        scheduleDO.setClassroomUuid(getData.getClassroomUuid());
        scheduleDO.setDayOfWeek(getData.getDayOfWeek());
        scheduleDO.setSectionStart(getData.getSectionStart());
        scheduleDO.setSectionEnd(getData.getSectionEnd());
        scheduleDO.setWeeksJson(getData.getWeeksJson());
        scheduleDO.setIsLocked(getData.getIsLocked() != null ? getData.getIsLocked() : false);
        scheduleDO.setStatus(getData.getStatus());
        scheduleDO.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        boolean saved = scheduleDAO.save(scheduleDO);
        if (!saved) {
            throw new BusinessException("保存排课失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("排课添加成功 - UUID: {}", scheduleDO.getScheduleUuid());
        return scheduleDO.getScheduleUuid();
    }

    @Override
    public PageDTO<ScheduleInfoDTO> getSchedulePage(int page, int size,
            String semesterUuid, String teachingClassUuid, String classroomUuid,
            String teacherUuid, Integer dayOfWeek, Integer status) {
        log.info("查询排课分页信息 - page: {}, size: {}, semester: {}, teachingClass: {}, classroom: {}, teacher: {}, day: {}, status: {}",
                page, size, semesterUuid, teachingClassUuid, classroomUuid, teacherUuid, dayOfWeek, status);

        // 调用DAO层进行分页查询
        IPage<ScheduleDO> pageResult = scheduleDAO.getSchedulePage(
                page, size, semesterUuid, teachingClassUuid, classroomUuid,
                teacherUuid, dayOfWeek, status);

        // 转换为 ScheduleInfoDTO
        List<ScheduleInfoDTO> scheduleInfoList = pageResult.getRecords().stream()
                .map(this::convertToScheduleInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<ScheduleInfoDTO> result = new PageDTO<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal((int) pageResult.getTotal());
        result.setRecords(scheduleInfoList);

        return result;
    }

    @Override
    public ScheduleInfoDTO getSchedule(String scheduleUuid) {
        log.info("获取排课信息 - UUID: {}", scheduleUuid);

        ScheduleDO schedule = scheduleDAO.getById(scheduleUuid);
        if (schedule == null) {
            throw new BusinessException("排课记录不存在: " + scheduleUuid, ErrorCode.OPERATION_FAILED);
        }

        return convertToScheduleInfoDTO(schedule);
    }

    @Override
    public void updateSchedule(AddScheduleVO getData) {
        log.info("更新排课信息 - UUID: {}, 学期: {}, 教学班: {}, 教室: {}",
                getData.getScheduleUuid(), getData.getSemesterUuid(),
                getData.getTeachingClassUuid(), getData.getClassroomUuid());

        // 查询排课记录是否存在
        ScheduleDO schedule = scheduleDAO.getById(getData.getScheduleUuid());
        if (schedule == null) {
            throw new BusinessException("排课记录不存在: " + getData.getScheduleUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查是否锁定
        if (Boolean.TRUE.equals(schedule.getIsLocked())) {
            throw new BusinessException("该排课记录已锁定，无法更新", ErrorCode.OPERATION_FAILED);
        }

        // 验证学期是否存在
        SemesterDO semesterDO = semesterDAO.getById(getData.getSemesterUuid());
        if (semesterDO == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教学班是否存在
        TeachingClassDO teachingClassDO = teachingClassDAO.getById(getData.getTeachingClassUuid());
        if (teachingClassDO == null) {
            throw new BusinessException("教学班不存在: " + getData.getTeachingClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教室是否存在
        ClassroomDO classroomDO = classroomDAO.getById(getData.getClassroomUuid());
        if (classroomDO == null) {
            throw new BusinessException("教室不存在: " + getData.getClassroomUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证时间参数
        if (getData.getDayOfWeek() == null || getData.getDayOfWeek() < 1 || getData.getDayOfWeek() > 7) {
            throw new BusinessException("星期几必须在1-7之间", ErrorCode.OPERATION_FAILED);
        }
        if (getData.getSectionStart() == null || getData.getSectionStart() < 1) {
            throw new BusinessException("起始节次必须大于0", ErrorCode.OPERATION_FAILED);
        }
        if (getData.getSectionEnd() == null || getData.getSectionEnd() < getData.getSectionStart()) {
            throw new BusinessException("结束节次必须大于等于起始节次", ErrorCode.OPERATION_FAILED);
        }

        // 验证weeksJson格式
        if (getData.getWeeksJson() == null || getData.getWeeksJson().trim().isEmpty()) {
            throw new BusinessException("上课周次不能为空", ErrorCode.OPERATION_FAILED);
        }
        try {
            JsonNode weeksNode = objectMapper.readTree(getData.getWeeksJson());
            // 验证周次是否超出学期范围
            int maxWeek = 0;
            for (JsonNode weekNode : weeksNode) {
                int week = weekNode.asInt();
                if (week > maxWeek) {
                    maxWeek = week;
                }
            }
            if (maxWeek > semesterDO.getSemesterWeeks()) {
                throw new BusinessException("上课周次不能超过学期周数(" + semesterDO.getSemesterWeeks() + "周)", ErrorCode.OPERATION_FAILED);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("上课周次格式错误，应为JSON数组格式", ErrorCode.OPERATION_FAILED);
        }

        // 验证状态
        if (getData.getStatus() == null || (getData.getStatus() != 0 && getData.getStatus() != 1)) {
            throw new BusinessException("状态必须为0（预览）或1（正式）", ErrorCode.OPERATION_FAILED);
        }

        // 更新排课信息
        schedule.setSemesterUuid(getData.getSemesterUuid());
        schedule.setTeachingClassUuid(getData.getTeachingClassUuid());
        schedule.setCourseUuid(teachingClassDO.getCourseUuid());
        schedule.setTeacherUuid(teachingClassDO.getTeacherUuid());
        schedule.setClassroomUuid(getData.getClassroomUuid());
        schedule.setDayOfWeek(getData.getDayOfWeek());
        schedule.setSectionStart(getData.getSectionStart());
        schedule.setSectionEnd(getData.getSectionEnd());
        schedule.setWeeksJson(getData.getWeeksJson());
        schedule.setIsLocked(getData.getIsLocked() != null ? getData.getIsLocked() : false);
        schedule.setStatus(getData.getStatus());
        schedule.setUpdatedAt(LocalDateTime.now());

        // 保存更新
        boolean updated = scheduleDAO.updateById(schedule);
        if (!updated) {
            throw new BusinessException("更新排课失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("排课更新成功 - UUID: {}", getData.getScheduleUuid());
    }

    @Override
    public void deleteSchedule(String scheduleUuid) {
        log.info("删除排课 - UUID: {}", scheduleUuid);

        // 查询排课记录是否存在
        ScheduleDO schedule = scheduleDAO.getById(scheduleUuid);
        if (schedule == null) {
            throw new BusinessException("排课记录不存在: " + scheduleUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查是否锁定
        if (Boolean.TRUE.equals(schedule.getIsLocked())) {
            throw new BusinessException("该排课记录已锁定，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = scheduleDAO.removeById(scheduleUuid);
        if (!deleted) {
            throw new BusinessException("删除排课失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("排课删除成功 - UUID: {}", scheduleUuid);
    }

    @Override
    public List<ScheduleInfoDTO> getTeacherTimetable(String teacherUuid, String semesterUuid) {
        log.info("查询教师课表 - 教师UUID: {}, 学期UUID: {}", teacherUuid, semesterUuid);

        // 验证教师是否存在
        TeacherDO teacherDO = teacherDAO.getById(teacherUuid);
        if (teacherDO == null) {
            throw new BusinessException("教师不存在: " + teacherUuid, ErrorCode.OPERATION_FAILED);
        }

        // 查询课表
        List<ScheduleDO> schedules = scheduleDAO.getTeacherTimetable(teacherUuid, semesterUuid);
        return schedules.stream()
                .map(this::convertToScheduleInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleInfoDTO> getStudentTimetable(String studentUuid, String semesterUuid) {
        log.info("查询学生课表 - 学生UUID: {}, 学期UUID: {}", studentUuid, semesterUuid);

        // 验证学生是否存在
        StudentDO studentDO = studentDAO.getById(studentUuid);
        if (studentDO == null) {
            throw new BusinessException("学生不存在: " + studentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 查询学生所属行政班的所有教学班
        List<TeachingClassClassDO> teachingClassClassList = teachingClassClassDAO.list(
                teachingClassClassDAO.lambdaQuery()
                        .eq(TeachingClassClassDO::getClassUuid, studentDO.getClassUuid())
                        .getWrapper()
        );

        if (teachingClassClassList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有教学班UUID
        List<String> teachingClassUuids = teachingClassClassList.stream()
                .map(TeachingClassClassDO::getTeachingClassUuid)
                .collect(Collectors.toList());

        // 查询所有教学班的课表
        List<ScheduleInfoDTO> result = new ArrayList<>();
        for (String teachingClassUuid : teachingClassUuids) {
            List<ScheduleDO> schedules = scheduleDAO.getTeachingClassTimetable(teachingClassUuid, semesterUuid);
            result.addAll(schedules.stream()
                    .map(this::convertToScheduleInfoDTO)
                    .collect(Collectors.toList()));
        }

        return result;
    }

    @Override
    public List<ScheduleInfoDTO> getClassTimetable(String classUuid, String semesterUuid) {
        log.info("查询行政班课表 - 行政班UUID: {}, 学期UUID: {}", classUuid, semesterUuid);

        // 查询行政班的所有教学班关联
        List<TeachingClassClassDO> teachingClassClassList = teachingClassClassDAO.list(
                teachingClassClassDAO.lambdaQuery()
                        .eq(TeachingClassClassDO::getClassUuid, classUuid)
                        .getWrapper()
        );

        if (teachingClassClassList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有教学班UUID
        List<String> teachingClassUuids = teachingClassClassList.stream()
                .map(TeachingClassClassDO::getTeachingClassUuid)
                .collect(Collectors.toList());

        // 查询所有教学班的课表
        List<ScheduleInfoDTO> result = new ArrayList<>();
        for (String teachingClassUuid : teachingClassUuids) {
            List<ScheduleDO> schedules = scheduleDAO.getTeachingClassTimetable(teachingClassUuid, semesterUuid);
            result.addAll(schedules.stream()
                    .map(this::convertToScheduleInfoDTO)
                    .collect(Collectors.toList()));
        }

        return result;
    }

    @Override
    public List<ScheduleInfoDTO> getClassroomTimetable(String classroomUuid, String semesterUuid) {
        log.info("查询教室课表 - 教室UUID: {}, 学期UUID: {}", classroomUuid, semesterUuid);

        // 验证教室是否存在
        ClassroomDO classroomDO = classroomDAO.getById(classroomUuid);
        if (classroomDO == null) {
            throw new BusinessException("教室不存在: " + classroomUuid, ErrorCode.OPERATION_FAILED);
        }

        // 查询课表
        List<ScheduleDO> schedules = scheduleDAO.getClassroomTimetable(classroomUuid, semesterUuid);
        return schedules.stream()
                .map(this::convertToScheduleInfoDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换 ScheduleDO 为 ScheduleInfoDTO
     */
    private ScheduleInfoDTO convertToScheduleInfoDTO(ScheduleDO scheduleDO) {
        ScheduleInfoDTO dto = new ScheduleInfoDTO();
        dto.setScheduleUuid(scheduleDO.getScheduleUuid());
        dto.setDayOfWeek(scheduleDO.getDayOfWeek());
        dto.setSectionStart(scheduleDO.getSectionStart());
        dto.setSectionEnd(scheduleDO.getSectionEnd());
        dto.setWeeksJson(scheduleDO.getWeeksJson());
        dto.setIsLocked(scheduleDO.getIsLocked());
        dto.setStatus(scheduleDO.getStatus());
        dto.setUpdatedAt(scheduleDO.getUpdatedAt());

        // 获取学期名称
        SemesterDO semesterDO = semesterDAO.getById(scheduleDO.getSemesterUuid());
        if (semesterDO != null) {
            dto.setSemesterName(semesterDO.getSemesterName());
        }

        // 获取教学班名称
        TeachingClassDO teachingClassDO = teachingClassDAO.getById(scheduleDO.getTeachingClassUuid());
        if (teachingClassDO != null) {
            dto.setTeachingClassName(teachingClassDO.getTeachingClassName());
        }

        // 获取课程名称
        CourseDO courseDO = courseDAO.getById(scheduleDO.getCourseUuid());
        if (courseDO != null) {
            dto.setCourseName(courseDO.getCourseName());
        }

        // 获取教师名称
        TeacherDO teacherDO = teacherDAO.getById(scheduleDO.getTeacherUuid());
        if (teacherDO != null) {
            dto.setTeacherName(teacherDO.getTeacherName());
        }

        // 获取教室名称
        ClassroomDO classroomDO = classroomDAO.getById(scheduleDO.getClassroomUuid());
        if (classroomDO != null) {
            dto.setClassroomName(classroomDO.getClassroomName());
        }

        return dto;
    }
}
