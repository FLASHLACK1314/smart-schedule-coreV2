package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseQualificationDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeachingClassDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseTypeDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseVO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;
    private final CourseTypeDAO courseTypeDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final CourseQualificationDAO courseQualificationDAO;

    @Override
    public String addCourse(AddCourseVO addCourseVO) {
        log.info("添加课程 - 编号: {}, 名称: {}, 类型UUID: {}",
                addCourseVO.getCourseNum(), addCourseVO.getCourseName(), addCourseVO.getCourseTypeUuid());

        // 检查课程类型是否存在
        CourseTypeDO courseType = courseTypeDAO.getById(addCourseVO.getCourseTypeUuid());
        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + addCourseVO.getCourseTypeUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查课程编号是否已存在
        if (courseDAO.existsByCourseNum(addCourseVO.getCourseNum())) {
            throw new BusinessException("课程编号已存在: " + addCourseVO.getCourseNum(), ErrorCode.OPERATION_FAILED);
        }

        // 创建课程对象
        CourseDO courseDO = new CourseDO();
        courseDO.setCourseUuid(UuidUtil.generateUuidNoDash());
        courseDO.setCourseNum(addCourseVO.getCourseNum());
        courseDO.setCourseName(addCourseVO.getCourseName());
        courseDO.setCourseTypeUuid(addCourseVO.getCourseTypeUuid());
        courseDO.setCourseCredit(addCourseVO.getCourseCredit());

        // 保存到数据库
        boolean saved = courseDAO.save(courseDO);

        if (!saved) {
            throw new BusinessException("保存课程失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程添加成功 - UUID: {}, 编号: {}, 名称: {}",
                courseDO.getCourseUuid(), addCourseVO.getCourseNum(), addCourseVO.getCourseName());

        return courseDO.getCourseUuid();
    }

    @Override
    public PageDTO<CourseInfoDTO> getCoursePage(int page, int size, String courseName, String courseNum, String courseTypeUuid) {
        log.info("查询课程分页信息 - page: {}, size: {}, courseName: {}, courseNum: {}, courseTypeUuid: {}",
                page, size, courseName, courseNum, courseTypeUuid);

        // 调用DAO层进行分页查询
        IPage<CourseDO> pageResult = courseDAO.getCoursePage(page, size, courseName, courseNum, courseTypeUuid);

        // 提取所有课程类型UUID
        Set<String> courseTypeUuids = pageResult.getRecords().stream()
                .map(CourseDO::getCourseTypeUuid)
                .collect(Collectors.toSet());

        // 批量查询课程类型信息
        List<CourseTypeDO> courseTypes = courseTypeDAO.listByIds(courseTypeUuids);
        Map<String, CourseTypeDO> courseTypeMap = courseTypes.stream()
                .collect(Collectors.toMap(CourseTypeDO::getCourseTypeUuid, ct -> ct));

        // 转换为DTO并填充类型名称
        List<CourseInfoDTO> courseInfoList = pageResult.getRecords().stream()
                .map(courseDO -> {
                    CourseInfoDTO dto = convertToCourseInfoDTO(courseDO);
                    CourseTypeDO courseType = courseTypeMap.get(courseDO.getCourseTypeUuid());
                    if (courseType != null) {
                        dto.setCourseTypeName(courseType.getTypeName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<CourseInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), courseInfoList);

        log.info("查询课程分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateCourse(AddCourseVO addCourseVO) {
        log.info("更新课程信息 - UUID: {}, 编号: {}, 名称: {}",
                addCourseVO.getCourseUuid(), addCourseVO.getCourseNum(), addCourseVO.getCourseName());

        // 查询课程是否存在
        CourseDO course = courseDAO.getById(addCourseVO.getCourseUuid());
        if (course == null) {
            throw new BusinessException("课程不存在: " + addCourseVO.getCourseUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查课程类型是否存在
        CourseTypeDO courseType = courseTypeDAO.getById(addCourseVO.getCourseTypeUuid());
        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + addCourseVO.getCourseTypeUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查课程编号是否被其他课程使用
        if (courseDAO.existsByCourseNumExcludeUuid(addCourseVO.getCourseNum(), addCourseVO.getCourseUuid())) {
            throw new BusinessException("课程编号已被其他课程使用: " + addCourseVO.getCourseNum(), ErrorCode.OPERATION_FAILED);
        }

        // 更新课程信息
        course.setCourseNum(addCourseVO.getCourseNum());
        course.setCourseName(addCourseVO.getCourseName());
        course.setCourseTypeUuid(addCourseVO.getCourseTypeUuid());
        course.setCourseCredit(addCourseVO.getCourseCredit());

        // 保存更新
        boolean updated = courseDAO.updateById(course);

        if (!updated) {
            throw new BusinessException("更新课程失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程更新成功 - UUID: {}, 编号: {}, 名称: {}",
                addCourseVO.getCourseUuid(), addCourseVO.getCourseNum(), addCourseVO.getCourseName());
    }

    @Override
    public CourseInfoDTO getCourse(String courseUuid) {
        log.info("获取课程信息 - UUID: {}", courseUuid);

        // 查询课程
        CourseDO course = courseDAO.getById(courseUuid);

        if (course == null) {
            throw new BusinessException("课程不存在: " + courseUuid, ErrorCode.OPERATION_FAILED);
        }

        // 查询课程类型信息
        CourseTypeDO courseType = courseTypeDAO.getById(course.getCourseTypeUuid());

        // 转换为 DTO 并返回
        CourseInfoDTO courseInfoDTO = convertToCourseInfoDTO(course);
        if (courseType != null) {
            courseInfoDTO.setCourseTypeName(courseType.getTypeName());
        }

        log.info("获取课程信息成功 - UUID: {}, 编号: {}, 名称: {}",
                courseUuid, course.getCourseNum(), course.getCourseName());

        return courseInfoDTO;
    }

    @Override
    public void deleteCourse(String courseUuid) {
        log.info("删除课程 - UUID: {}", courseUuid);

        // 查询课程是否存在
        CourseDO course = courseDAO.getById(courseUuid);
        if (course == null) {
            throw new BusinessException("课程不存在: " + courseUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查课程是否被教学班引用
        if (teachingClassDAO.existsByCourseUuid(courseUuid)) {
            long teachingClassCount = teachingClassDAO.countByCourseUuid(courseUuid);
            throw new BusinessException("该课程还有 " + teachingClassCount + " 个教学班，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 检查课程是否被课程资格关联表引用
        if (courseQualificationDAO.existsByCourseUuid(courseUuid)) {
            long qualificationCount = courseQualificationDAO.countByCourseUuid(courseUuid);
            throw new BusinessException("该课程还有 " + qualificationCount + " 条课程资格记录，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // TODO: 检查课程是否被排课记录引用
        // 这部分可以后续完善，等排课功能实现后再补充

        // 执行删除
        boolean deleted = courseDAO.removeById(courseUuid);

        if (!deleted) {
            throw new BusinessException("删除课程失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程删除成功 - UUID: {}, 编号: {}, 名称: {}",
                courseUuid, course.getCourseNum(), course.getCourseName());
    }

    /**
     * 转换 CourseDO 为 CourseInfoDTO
     *
     * @param courseDO 课程实体
     * @return 课程信息DTO
     */
    private CourseInfoDTO convertToCourseInfoDTO(CourseDO courseDO) {
        CourseInfoDTO dto = new CourseInfoDTO();
        dto.setCourseUuid(courseDO.getCourseUuid());
        dto.setCourseNum(courseDO.getCourseNum());
        dto.setCourseName(courseDO.getCourseName());
        dto.setCourseTypeUuid(courseDO.getCourseTypeUuid());
        dto.setCourseCredit(courseDO.getCourseCredit());
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
    private PageDTO<CourseInfoDTO> buildPageDTO(int page, int size, int total, List<CourseInfoDTO> records) {
        PageDTO<CourseInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
