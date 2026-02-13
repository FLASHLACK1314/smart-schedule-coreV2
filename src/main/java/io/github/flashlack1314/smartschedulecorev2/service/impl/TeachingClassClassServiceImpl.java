package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 教学班-行政班关联服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingClassClassServiceImpl implements TeachingClassClassService {
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final MajorDAO majorDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public void addTeachingClassClass(String teachingClassUuid, String classUuid) {
        log.info("添加教学班-行政班关联 - 教学班UUID: {}, 行政班UUID: {}", teachingClassUuid, classUuid);

        // 验证教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(teachingClassUuid);
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + teachingClassUuid, ErrorCode.OPERATION_FAILED);
        }

        // 验证行政班是否存在
        ClassDO classDO = classDAO.getById(classUuid);
        if (classDO == null) {
            throw new BusinessException("行政班不存在: " + classUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查关联是否已存在
        if (teachingClassClassDAO.existsByTeachingClassUuidAndClassUuid(teachingClassUuid, classUuid)) {
            throw new BusinessException("该教学班已包含此行政班", ErrorCode.OPERATION_FAILED);
        }

        // 创建关联对象
        TeachingClassClassDO teachingClassClass = new TeachingClassClassDO();
        teachingClassClass.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash());
        teachingClassClass.setTeachingClassUuid(teachingClassUuid);
        teachingClassClass.setClassUuid(classUuid);

        // 保存到数据库
        boolean saved = teachingClassClassDAO.save(teachingClassClass);

        if (!saved) {
            throw new BusinessException("保存教学班-行政班关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学班-行政班关联添加成功 - 关联UUID: {}, 教学班: {}, 行政班: {}",
                teachingClassClass.getTeachingClassClassUuid(),
                teachingClass.getTeachingClassName(),
                classDO.getClassName());
    }

    @Override
    public PageDTO<TeachingClassClassInfoDTO> getTeachingClassClassPage(int page, int size,
            String teachingClassUuid, String classUuid) {
        log.info("查询教学班-行政班关联分页信息 - page: {}, size: {}, teachingClassUuid: {}, classUuid: {}",
                page, size, teachingClassUuid, classUuid);

        // 调用DAO层进行分页查询
        IPage<TeachingClassClassDO> pageResult = teachingClassClassDAO.getTeachingClassClassPage(
                page, size, teachingClassUuid, classUuid);

        // 提取所有教学班UUID和行政班UUID
        Set<String> teachingClassUuids = pageResult.getRecords().stream()
                .map(TeachingClassClassDO::getTeachingClassUuid)
                .collect(Collectors.toSet());
        Set<String> classUuids = pageResult.getRecords().stream()
                .map(TeachingClassClassDO::getClassUuid)
                .collect(Collectors.toSet());

        // 批量查询教学班信息
        List<TeachingClassDO> teachingClasses = teachingClassDAO.listByIds(teachingClassUuids);
        Map<String, TeachingClassDO> teachingClassMap = teachingClasses.stream()
                .collect(Collectors.toMap(TeachingClassDO::getTeachingClassUuid, tc -> tc));

        // 批量查询行政班信息
        List<ClassDO> classes = classDAO.listByIds(classUuids);
        Map<String, ClassDO> classMap = classes.stream()
                .collect(Collectors.toMap(ClassDO::getClassUuid, c -> c));

        // 提取所有课程UUID、教师UUID、专业UUID
        Set<String> courseUuids = teachingClasses.stream()
                .map(TeachingClassDO::getCourseUuid)
                .collect(Collectors.toSet());
        Set<String> teacherUuids = teachingClasses.stream()
                .map(TeachingClassDO::getTeacherUuid)
                .collect(Collectors.toSet());
        Set<String> majorUuids = classes.stream()
                .map(ClassDO::getMajorUuid)
                .collect(Collectors.toSet());

        // 批量查询课程信息
        List<CourseDO> courses = courseDAO.listByIds(courseUuids);
        Map<String, CourseDO> courseMap = courses.stream()
                .collect(Collectors.toMap(CourseDO::getCourseUuid, c -> c));

        // 批量查询教师信息
        List<TeacherDO> teachers = teacherDAO.listByIds(teacherUuids);
        Map<String, TeacherDO> teacherMap = teachers.stream()
                .collect(Collectors.toMap(TeacherDO::getTeacherUuid, t -> t));

        // 批量查询专业信息
        List<MajorDO> majors = majorDAO.listByIds(majorUuids);
        Map<String, MajorDO> majorMap = majors.stream()
                .collect(Collectors.toMap(MajorDO::getMajorUuid, m -> m));

        // 提取所有学院UUID
        Set<String> departmentUuids = majors.stream()
                .map(MajorDO::getDepartmentUuid)
                .collect(Collectors.toSet());

        // 批量查询学院信息
        List<DepartmentDO> departments = departmentDAO.listByIds(departmentUuids);
        Map<String, DepartmentDO> departmentMap = departments.stream()
                .collect(Collectors.toMap(DepartmentDO::getDepartmentUuid, d -> d));

        // 转换为DTO并填充关联信息
        List<TeachingClassClassInfoDTO> teachingClassClassInfoList = pageResult.getRecords().stream()
                .map(teachingClassClass -> {
                    TeachingClassClassInfoDTO dto = convertToTeachingClassClassInfoDTO(teachingClassClass);

                    // 填充教学班信息
                    TeachingClassDO teachingClass = teachingClassMap.get(teachingClassClass.getTeachingClassUuid());
                    if (teachingClass != null) {
                        dto.setTeachingClassName(teachingClass.getTeachingClassName());
                        dto.setCourseUuid(teachingClass.getCourseUuid());
                        dto.setTeacherUuid(teachingClass.getTeacherUuid());

                        // 填充课程信息
                        CourseDO course = courseMap.get(teachingClass.getCourseUuid());
                        if (course != null) {
                            dto.setCourseName(course.getCourseName());
                        }

                        // 填充教师信息
                        TeacherDO teacher = teacherMap.get(teachingClass.getTeacherUuid());
                        if (teacher != null) {
                            dto.setTeacherName(teacher.getTeacherName());
                        }
                    }

                    // 填充行政班信息
                    ClassDO classEntity = classMap.get(teachingClassClass.getClassUuid());
                    if (classEntity != null) {
                        dto.setClassName(classEntity.getClassName());
                        dto.setMajorUuid(classEntity.getMajorUuid());

                        // 填充专业信息
                        MajorDO major = majorMap.get(classEntity.getMajorUuid());
                        if (major != null) {
                            dto.setMajorName(major.getMajorName());
                            dto.setDepartmentUuid(major.getDepartmentUuid());

                            // 填充学院信息
                            DepartmentDO department = departmentMap.get(major.getDepartmentUuid());
                            if (department != null) {
                                dto.setDepartmentName(department.getDepartmentName());
                            }
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<TeachingClassClassInfoDTO> result = buildPageDTO(page, size,
                (int) pageResult.getTotal(), teachingClassClassInfoList);

        log.info("查询教学班-行政班关联分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void deleteTeachingClassClass(String teachingClassClassUuid) {
        log.info("删除教学班-行政班关联 - 关联UUID: {}", teachingClassClassUuid);

        // 查询关联是否存在
        TeachingClassClassDO teachingClassClass = teachingClassClassDAO.getById(teachingClassClassUuid);
        if (teachingClassClass == null) {
            throw new BusinessException("教学班-行政班关联不存在: " + teachingClassClassUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = teachingClassClassDAO.removeById(teachingClassClassUuid);

        if (!deleted) {
            throw new BusinessException("删除教学班-行政班关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教学班-行政班关联删除成功 - 关联UUID: {}", teachingClassClassUuid);
    }

    /**
     * 转换 TeachingClassClassDO 为 TeachingClassClassInfoDTO
     *
     * @param teachingClassClass 教学班-行政班关联实体
     * @return 教学班-行政班关联信息DTO
     */
    private TeachingClassClassInfoDTO convertToTeachingClassClassInfoDTO(TeachingClassClassDO teachingClassClass) {
        TeachingClassClassInfoDTO dto = new TeachingClassClassInfoDTO();
        dto.setTeachingClassClassUuid(teachingClassClass.getTeachingClassClassUuid());
        dto.setTeachingClassUuid(teachingClassClass.getTeachingClassUuid());
        dto.setClassUuid(teachingClassClass.getClassUuid());
        return dto;
    }

    /**
     * 构建分页DTO
     *
     * @param page    页码
     * @param size    每页数量
     * @param total   总数
     * @param records 记录列表
     * @return 分页DTO
     */
    private PageDTO<TeachingClassClassInfoDTO> buildPageDTO(int page, int size, int total,
            List<TeachingClassClassInfoDTO> records) {
        PageDTO<TeachingClassClassInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
