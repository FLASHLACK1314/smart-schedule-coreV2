package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleConflictInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.service.ScheduleConflictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排课冲突服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleConflictServiceImpl implements ScheduleConflictService {
    private final ScheduleConflictDAO scheduleConflictDAO;
    private final ScheduleDAO scheduleDAO;
    private final SemesterDAO semesterDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final ClassroomDAO classroomDAO;

    @Override
    public PageDTO<ScheduleConflictInfoDTO> getConflictPage(int page, int size, String semesterUuid,
                                                             Integer severity, String conflictType) {
        log.info("查询排课冲突分页信息 - page: {}, size: {}, semester: {}, severity: {}, conflictType: {}",
                page, size, semesterUuid, severity, conflictType);

        // 构建查询条件
        var queryWrapper = scheduleConflictDAO.lambdaQuery()
                .eq(StringUtils.hasText(semesterUuid), ScheduleConflictDO::getSemesterUuid, semesterUuid)
                .eq(severity != null, ScheduleConflictDO::getSeverity, severity)
                .eq(StringUtils.hasText(conflictType), ScheduleConflictDO::getConflictType, conflictType)
                .orderByDesc(ScheduleConflictDO::getSeverity)
                .orderByDesc(ScheduleConflictDO::getConflictUuid)
                .getWrapper();

        // 执行分页查询
        IPage<ScheduleConflictDO> pageResult = scheduleConflictDAO.page(new Page<>(page, size), queryWrapper);

        // 转换为 DTO
        List<ScheduleConflictInfoDTO> records = pageResult.getRecords().stream()
                .map(this::convertToConflictInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<ScheduleConflictInfoDTO> result = new PageDTO<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal((int) pageResult.getTotal());
        result.setRecords(records);

        return result;
    }

    @Override
    public ScheduleConflictInfoDTO getConflict(String conflictUuid) {
        log.info("获取冲突详情 - UUID: {}", conflictUuid);

        ScheduleConflictDO conflict = scheduleConflictDAO.getById(conflictUuid);
        if (conflict == null) {
            throw new BusinessException("冲突记录不存在: " + conflictUuid, ErrorCode.OPERATION_FAILED);
        }

        return convertToConflictInfoDTO(conflict);
    }

    @Override
    public Map<String, Object> getConflictStats(String semesterUuid) {
        log.info("获取冲突统计信息 - semesterUuid: {}", semesterUuid);

        var queryWrapper = scheduleConflictDAO.lambdaQuery()
                .eq(StringUtils.hasText(semesterUuid), ScheduleConflictDO::getSemesterUuid, semesterUuid)
                .getWrapper();

        List<ScheduleConflictDO> conflicts = scheduleConflictDAO.list(queryWrapper);

        // 统计总数
        int totalCount = conflicts.size();

        // 统计硬冲突数量
        int hardConflictCount = (int) conflicts.stream()
                .filter(c -> c.getSeverity() != null && c.getSeverity() == 1)
                .count();

        // 统计软冲突数量
        int softConflictCount = totalCount - hardConflictCount;

        // 按冲突类型统计
        Map<String, Long> conflictTypeStats = conflicts.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getConflictType() != null ? c.getConflictType() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalCount);
        result.put("hard_conflict_count", hardConflictCount);
        result.put("soft_conflict_count", softConflictCount);
        result.put("conflict_type_stats", conflictTypeStats);

        return result;
    }

    @Override
    public void deleteConflict(String conflictUuid) {
        log.info("删除冲突记录 - UUID: {}", conflictUuid);

        ScheduleConflictDO conflict = scheduleConflictDAO.getById(conflictUuid);
        if (conflict == null) {
            throw new BusinessException("冲突记录不存在: " + conflictUuid, ErrorCode.OPERATION_FAILED);
        }

        boolean removed = scheduleConflictDAO.removeById(conflictUuid);
        if (!removed) {
            throw new BusinessException("删除冲突记录失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("冲突记录删除成功 - UUID: {}", conflictUuid);
    }

    @Override
    public List<ScheduleConflictInfoDTO> getConflictsByScheduleUuid(String scheduleUuid) {
        log.info("根据排课UUID查询冲突 - scheduleUuid: {}", scheduleUuid);

        List<ScheduleConflictDO> conflicts = scheduleConflictDAO.lambdaQuery()
                .eq(ScheduleConflictDO::getScheduleUuidA, scheduleUuid)
                .or()
                .eq(ScheduleConflictDO::getScheduleUuidB, scheduleUuid)
                .list();

        return conflicts.stream()
                .map(this::convertToConflictInfoDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换 ScheduleConflictDO 为 ScheduleConflictInfoDTO
     */
    private ScheduleConflictInfoDTO convertToConflictInfoDTO(ScheduleConflictDO conflict) {
        ScheduleConflictInfoDTO dto = new ScheduleConflictInfoDTO();
        dto.setConflictUuid(conflict.getConflictUuid());
        dto.setScheduleUuidA(conflict.getScheduleUuidA());
        dto.setScheduleUuidB(conflict.getScheduleUuidB());
        dto.setConflictType(conflict.getConflictType());
        dto.setSeverity(conflict.getSeverity());
        dto.setDescription(conflict.getDescription());

        // 获取学期名称
        if (StringUtils.hasText(conflict.getSemesterUuid())) {
            SemesterDO semester = semesterDAO.getById(conflict.getSemesterUuid());
            if (semester != null) {
                dto.setSemesterName(semester.getSemesterName());
            }
        }

        // 获取排课A的详细信息
        if (StringUtils.hasText(conflict.getScheduleUuidA())) {
            fillScheduleAInfo(dto, conflict.getScheduleUuidA());
        }

        // 获取排课B的详细信息
        if (StringUtils.hasText(conflict.getScheduleUuidB())) {
            fillScheduleBInfo(dto, conflict.getScheduleUuidB());
        }

        return dto;
    }

    /**
     * 填充排课A的详细信息
     */
    private void fillScheduleAInfo(ScheduleConflictInfoDTO dto, String scheduleUuidA) {
        ScheduleDO scheduleA = scheduleDAO.getById(scheduleUuidA);
        if (scheduleA == null) {
            return;
        }

        dto.setDayOfWeekA(scheduleA.getDayOfWeek());
        dto.setSectionStartA(scheduleA.getSectionStart());
        dto.setSectionEndA(scheduleA.getSectionEnd());

        // 获取教学班名称
        if (StringUtils.hasText(scheduleA.getTeachingClassUuid())) {
            TeachingClassDO teachingClass = teachingClassDAO.getById(scheduleA.getTeachingClassUuid());
            if (teachingClass != null) {
                dto.setTeachingClassNameA(teachingClass.getTeachingClassName());
            }
        }

        // 获取课程名称
        if (StringUtils.hasText(scheduleA.getCourseUuid())) {
            CourseDO course = courseDAO.getById(scheduleA.getCourseUuid());
            if (course != null) {
                dto.setCourseNameA(course.getCourseName());
            }
        }

        // 获取教师名称
        if (StringUtils.hasText(scheduleA.getTeacherUuid())) {
            TeacherDO teacher = teacherDAO.getById(scheduleA.getTeacherUuid());
            if (teacher != null) {
                dto.setTeacherNameA(teacher.getTeacherName());
            }
        }

        // 获取教室名称
        if (StringUtils.hasText(scheduleA.getClassroomUuid())) {
            ClassroomDO classroom = classroomDAO.getById(scheduleA.getClassroomUuid());
            if (classroom != null) {
                dto.setClassroomNameA(classroom.getClassroomName());
            }
        }
    }

    /**
     * 填充排课B的详细信息
     */
    private void fillScheduleBInfo(ScheduleConflictInfoDTO dto, String scheduleUuidB) {
        ScheduleDO scheduleB = scheduleDAO.getById(scheduleUuidB);
        if (scheduleB == null) {
            return;
        }

        dto.setDayOfWeekB(scheduleB.getDayOfWeek());
        dto.setSectionStartB(scheduleB.getSectionStart());
        dto.setSectionEndB(scheduleB.getSectionEnd());

        // 获取教学班名称
        if (StringUtils.hasText(scheduleB.getTeachingClassUuid())) {
            TeachingClassDO teachingClass = teachingClassDAO.getById(scheduleB.getTeachingClassUuid());
            if (teachingClass != null) {
                dto.setTeachingClassNameB(teachingClass.getTeachingClassName());
            }
        }

        // 获取课程名称
        if (StringUtils.hasText(scheduleB.getCourseUuid())) {
            CourseDO course = courseDAO.getById(scheduleB.getCourseUuid());
            if (course != null) {
                dto.setCourseNameB(course.getCourseName());
            }
        }

        // 获取教师名称
        if (StringUtils.hasText(scheduleB.getTeacherUuid())) {
            TeacherDO teacher = teacherDAO.getById(scheduleB.getTeacherUuid());
            if (teacher != null) {
                dto.setTeacherNameB(teacher.getTeacherName());
            }
        }

        // 获取教室名称
        if (StringUtils.hasText(scheduleB.getClassroomUuid())) {
            ClassroomDO classroom = classroomDAO.getById(scheduleB.getClassroomUuid());
            if (classroom != null) {
                dto.setClassroomNameB(classroom.getClassroomName());
            }
        }
    }
}
