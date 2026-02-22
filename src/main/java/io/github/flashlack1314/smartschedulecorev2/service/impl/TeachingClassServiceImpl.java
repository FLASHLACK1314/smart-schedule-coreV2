package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SemesterDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeachingClassDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddTeachingClassVO;
import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教学班服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingClassServiceImpl implements TeachingClassService {
    private final TeachingClassDAO teachingClassDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final ScheduleDAO scheduleDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final SemesterDAO semesterDAO;
    private final CourseQualificationDAO courseQualificationDAO;

    @Override
    public String addTeachingClass(AddTeachingClassVO getData) {
        log.info("添加教学班 - 课程UUID: {}, 教师UUID: {}, 学期UUID: {}, 名称: {}",
                getData.getCourseUuid(), getData.getTeacherUuid(),
                getData.getSemesterUuid(), getData.getTeachingClassName());

        // 验证课程是否存在
        CourseDO courseDO = courseDAO.getById(getData.getCourseUuid());
        if (courseDO == null) {
            throw new BusinessException("课程不存在: " + getData.getCourseUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教师是否存在
        TeacherDO teacherDO = teacherDAO.getById(getData.getTeacherUuid());
        if (teacherDO == null) {
            throw new BusinessException("教师不存在: " + getData.getTeacherUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证学期是否存在
        SemesterDO semesterDO = semesterDAO.getById(getData.getSemesterUuid());
        if (semesterDO == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教师是否有该课程的授课资格
        if (!courseQualificationDAO.existsByCourseUuidAndTeacherUuid(
                getData.getCourseUuid(), getData.getTeacherUuid())) {
            throw new BusinessException(
                    "该教师没有此课程的授课资格",
                    ErrorCode.OPERATION_FAILED
            );
        }

        // 创建教学班对象
        TeachingClassDO teachingClassDO = new TeachingClassDO();
        teachingClassDO.setTeachingClassUuid(UuidUtil.generateUuidNoDash());
        teachingClassDO.setCourseUuid(getData.getCourseUuid());
        teachingClassDO.setTeacherUuid(getData.getTeacherUuid());
        teachingClassDO.setSemesterUuid(getData.getSemesterUuid());
        teachingClassDO.setTeachingClassName(getData.getTeachingClassName());

        // 保存到数据库
        boolean saved = teachingClassDAO.save(teachingClassDO);

        if (!saved) {
            throw new BusinessException("保存教学班失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学班添加成功 - UUID: {}, 名称: {}",
                teachingClassDO.getTeachingClassUuid(), getData.getTeachingClassName());

        return teachingClassDO.getTeachingClassUuid();
    }

    @Override
    public PageDTO<TeachingClassInfoDTO> getTeachingClassPage(int page, int size, String courseUuid,
                                                                String teacherUuid, String semesterUuid) {
        log.info("查询教学班分页信息 - page: {}, size: {}, courseUuid: {}, teacherUuid: {}, semesterUuid: {}",
                page, size, courseUuid, teacherUuid, semesterUuid);

        // 调用DAO层进行分页查询
        IPage<TeachingClassDO> pageResult = teachingClassDAO.getTeachingClassPage(
                page, size, courseUuid, teacherUuid, semesterUuid);

        // 转换为 TeachingClassInfoDTO
        List<TeachingClassInfoDTO> teachingClassInfoList = pageResult.getRecords().stream()
                .map(this::convertToTeachingClassInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<TeachingClassInfoDTO> result = new PageDTO<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal((int) pageResult.getTotal());
        result.setRecords(teachingClassInfoList);

        return result;
    }

    @Override
    public TeachingClassInfoDTO getTeachingClass(String teachingClassUuid) {
        log.info("获取教学班信息 - UUID: {}", teachingClassUuid);

        TeachingClassDO teachingClass = teachingClassDAO.getById(teachingClassUuid);
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + teachingClassUuid, ErrorCode.OPERATION_FAILED);
        }

        return convertToTeachingClassInfoDTO(teachingClass);
    }

    @Override
    public void updateTeachingClass(AddTeachingClassVO getData) {
        log.info("更新教学班信息 - UUID: {}, 课程UUID: {}, 教师UUID: {}, 学期UUID: {}",
                getData.getTeachingClassUuid(), getData.getCourseUuid(),
                getData.getTeacherUuid(), getData.getSemesterUuid());

        // 查询教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(getData.getTeachingClassUuid());
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + getData.getTeachingClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证课程是否存在
        CourseDO courseDO = courseDAO.getById(getData.getCourseUuid());
        if (courseDO == null) {
            throw new BusinessException("课程不存在: " + getData.getCourseUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教师是否存在
        TeacherDO teacherDO = teacherDAO.getById(getData.getTeacherUuid());
        if (teacherDO == null) {
            throw new BusinessException("教师不存在: " + getData.getTeacherUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证学期是否存在
        SemesterDO semesterDO = semesterDAO.getById(getData.getSemesterUuid());
        if (semesterDO == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教师是否有该课程的授课资格
        if (!courseQualificationDAO.existsByCourseUuidAndTeacherUuid(
                getData.getCourseUuid(), getData.getTeacherUuid())) {
            throw new BusinessException(
                    "该教师没有此课程的授课资格",
                    ErrorCode.OPERATION_FAILED
            );
        }

        // 更新教学班信息
        teachingClass.setCourseUuid(getData.getCourseUuid());
        teachingClass.setTeacherUuid(getData.getTeacherUuid());
        teachingClass.setSemesterUuid(getData.getSemesterUuid());
        teachingClass.setTeachingClassName(getData.getTeachingClassName());

        // 保存更新
        boolean updated = teachingClassDAO.updateById(teachingClass);
        if (!updated) {
            throw new BusinessException("更新教学班失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学班更新成功 - UUID: {}, 名称: {}",
                getData.getTeachingClassUuid(), getData.getTeachingClassName());
    }

    @Override
    public void deleteTeachingClass(String teachingClassUuid) {
        log.info("删除教学班 - UUID: {}", teachingClassUuid);

        // 查询教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(teachingClassUuid);
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + teachingClassUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查是否被 teaching_class_class 表引用
        long classCount = teachingClassClassDAO.countByTeachingClassUuid(teachingClassUuid);
        if (classCount > 0) {
            throw new BusinessException(
                    "该教学班被 " + classCount + " 个行政班关联，无法删除",
                    ErrorCode.OPERATION_FAILED
            );
        }

        // 检查是否被 schedule 表引用
        long scheduleCount = scheduleDAO.countByTeachingClassUuid(teachingClassUuid);
        if (scheduleCount > 0) {
            throw new BusinessException(
                    "该教学班被 " + scheduleCount + " 个排课记录引用，无法删除",
                    ErrorCode.OPERATION_FAILED
            );
        }

        // 执行删除
        boolean deleted = teachingClassDAO.removeById(teachingClassUuid);
        if (!deleted) {
            throw new BusinessException("删除教学班失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学班删除成功 - UUID: {}, 名称: {}",
                teachingClassUuid, teachingClass.getTeachingClassName());
    }

    /**
     * 转换 TeachingClassDO 为 TeachingClassInfoDTO
     */
    private TeachingClassInfoDTO convertToTeachingClassInfoDTO(TeachingClassDO teachingClassDO) {
        TeachingClassInfoDTO dto = new TeachingClassInfoDTO();
        dto.setTeachingClassUuid(teachingClassDO.getTeachingClassUuid());
        dto.setTeachingClassName(teachingClassDO.getTeachingClassName());
        dto.setTeachingClassHours(teachingClassDO.getTeachingClassHours());  // 修复：添加学时字段映射

        // 获取课程名称
        CourseDO courseDO = courseDAO.getById(teachingClassDO.getCourseUuid());
        if (courseDO != null) {
            dto.setCourseName(courseDO.getCourseName());
        }

        // 获取教师名称
        TeacherDO teacherDO = teacherDAO.getById(teachingClassDO.getTeacherUuid());
        if (teacherDO != null) {
            dto.setTeacherName(teacherDO.getTeacherName());
        }

        // 获取学期名称
        SemesterDO semesterDO = semesterDAO.getById(teachingClassDO.getSemesterUuid());
        if (semesterDO != null) {
            dto.setSemesterName(semesterDO.getSemesterName());
        }

        return dto;
    }
}
