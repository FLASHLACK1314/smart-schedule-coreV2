package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseQualificationDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeacherDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseQualificationInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseQualificationDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseQualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程教师资格关联服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseQualificationServiceImpl implements CourseQualificationService {
    private final CourseQualificationDAO courseQualificationDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public void addQualification(String courseUuid, String teacherUuid) {
        log.info("添加课程-教师资格关联 - 课程UUID: {}, 教师UUID: {}", courseUuid, teacherUuid);

        // 验证课程是否存在
        CourseDO course = courseDAO.getById(courseUuid);
        if (course == null) {
            throw new BusinessException("课程不存在: " + courseUuid, ErrorCode.OPERATION_FAILED);
        }

        // 验证教师是否存在
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        if (teacher == null) {
            throw new BusinessException("教师不存在: " + teacherUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查关联是否已存在
        if (courseQualificationDAO.existsByCourseUuidAndTeacherUuid(courseUuid, teacherUuid)) {
            throw new BusinessException("该教师已有此课程资格", ErrorCode.OPERATION_FAILED);
        }

        // 创建关联对象
        CourseQualificationDO qualification = new CourseQualificationDO();
        qualification.setCourseQualificationUuid(UuidUtil.generateUuidNoDash());
        qualification.setCourseUuid(courseUuid);
        qualification.setTeacherUuid(teacherUuid);

        // 保存到数据库
        boolean saved = courseQualificationDAO.save(qualification);

        if (!saved) {
            throw new BusinessException("保存资格关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程-教师资格关联添加成功 - 关联UUID: {}, 课程: {}, 教师: {}",
                qualification.getCourseQualificationUuid(), course.getCourseName(), teacher.getTeacherName());
    }

    @Override
    public PageDTO<CourseQualificationInfoDTO> getQualificationPage(int page, int size,
                                                                    String courseUuid, String teacherUuid) {
        log.info("查询课程-教师资格关联分页信息 - page: {}, size: {}, courseUuid: {}, teacherUuid: {}",
                page, size, courseUuid, teacherUuid);

        // 调用DAO层进行分页查询
        IPage<CourseQualificationDO> pageResult = courseQualificationDAO.getQualificationPage(
                page, size, courseUuid, teacherUuid);

        // 提取所有课程UUID和教师UUID
        Set<String> courseUuids = pageResult.getRecords().stream()
                .map(CourseQualificationDO::getCourseUuid)
                .collect(Collectors.toSet());
        Set<String> teacherUuids = pageResult.getRecords().stream()
                .map(CourseQualificationDO::getTeacherUuid)
                .collect(Collectors.toSet());

        // 批量查询课程信息
        List<CourseDO> courses = courseDAO.listByIds(courseUuids);
        Map<String, CourseDO> courseMap = courses.stream()
                .collect(Collectors.toMap(CourseDO::getCourseUuid, c -> c));

        // 批量查询教师信息
        List<TeacherDO> teachers = teacherDAO.listByIds(teacherUuids);
        Map<String, TeacherDO> teacherMap = teachers.stream()
                .collect(Collectors.toMap(TeacherDO::getTeacherUuid, t -> t));

        // 提取所有学院UUID
        Set<String> departmentUuids = teachers.stream()
                .map(TeacherDO::getDepartmentUuid)
                .collect(Collectors.toSet());

        // 批量查询学院信息
        List<DepartmentDO> departments = departmentDAO.listByIds(departmentUuids);
        Map<String, DepartmentDO> departmentMap = departments.stream()
                .collect(Collectors.toMap(DepartmentDO::getDepartmentUuid, d -> d));

        // 转换为DTO并填充关联信息
        List<CourseQualificationInfoDTO> qualificationInfoList = pageResult.getRecords().stream()
                .map(qualification -> {
                    CourseQualificationInfoDTO dto = convertToQualificationInfoDTO(qualification);

                    // 填充课程信息
                    CourseDO course = courseMap.get(qualification.getCourseUuid());
                    if (course != null) {
                        dto.setCourseName(course.getCourseName());
                    }

                    // 填充教师信息
                    TeacherDO teacher = teacherMap.get(qualification.getTeacherUuid());
                    if (teacher != null) {
                        dto.setTeacherName(teacher.getTeacherName());
                        dto.setTeacherTitle(teacher.getTitle());
                        dto.setDepartmentUuid(teacher.getDepartmentUuid());

                        // 填充学院信息
                        DepartmentDO department = departmentMap.get(teacher.getDepartmentUuid());
                        if (department != null) {
                            dto.setDepartmentName(department.getDepartmentName());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<CourseQualificationInfoDTO> result = buildPageDTO(page, size,
                (int) pageResult.getTotal(), qualificationInfoList);

        log.info("查询课程-教师资格关联分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void deleteQualification(String qualificationUuid) {
        log.info("删除课程-教师资格关联 - 关联UUID: {}", qualificationUuid);

        // 查询关联是否存在
        CourseQualificationDO qualification = courseQualificationDAO.getById(qualificationUuid);
        if (qualification == null) {
            throw new BusinessException("资格关联不存在: " + qualificationUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = courseQualificationDAO.removeById(qualificationUuid);

        if (!deleted) {
            throw new BusinessException("删除资格关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程-教师资格关联删除成功 - 关联UUID: {}", qualificationUuid);
    }

    /**
     * 转换 CourseQualificationDO 为 CourseQualificationInfoDTO
     *
     * @param qualification 资格关联实体
     * @return 资格关联信息DTO
     */
    private CourseQualificationInfoDTO convertToQualificationInfoDTO(CourseQualificationDO qualification) {
        CourseQualificationInfoDTO dto = new CourseQualificationInfoDTO();
        dto.setCourseQualificationUuid(qualification.getCourseQualificationUuid());
        dto.setCourseUuid(qualification.getCourseUuid());
        dto.setTeacherUuid(qualification.getTeacherUuid());
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
    private PageDTO<CourseQualificationInfoDTO> buildPageDTO(int page, int size, int total,
                                                              List<CourseQualificationInfoDTO> records) {
        PageDTO<CourseQualificationInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
