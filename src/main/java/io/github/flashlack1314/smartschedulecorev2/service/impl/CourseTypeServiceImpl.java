package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseTypeDO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程类型服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTypeServiceImpl implements CourseTypeService {
    private final CourseTypeDAO courseTypeDAO;

    @Override
    public void addCourseType(String courseTypeName) {
        log.info("添加课程类型 - 名称: {}", courseTypeName);

        // 检查课程类型名称是否已存在
        if (courseTypeDAO.existsByCourseTypeName(courseTypeName)) {
            throw new BusinessException("课程类型名称已存在: " + courseTypeName, ErrorCode.OPERATION_FAILED);
        }

        // 创建课程类型对象
        CourseTypeDO courseTypeDO = new CourseTypeDO();
        courseTypeDO.setCourseTypeUuid(UuidUtil.generateUuidNoDash());
        courseTypeDO.setTypeName(courseTypeName);

        // 保存到数据库
        boolean saved = courseTypeDAO.save(courseTypeDO);

        if (!saved) {
            throw new BusinessException("保存课程类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程类型添加成功 - UUID: {}, 名称: {}", courseTypeDO.getCourseTypeUuid(), courseTypeName);
    }

    @Override
    public PageDTO<CourseTypeInfoDTO> getCourseTypePage(int page, int size, String courseTypeName) {
        log.info("查询课程类型分页信息 - page: {}, size: {}, courseTypeName: {}",
                page, size, courseTypeName);

        // 调用DAO层进行分页查询
        IPage<CourseTypeDO> pageResult = courseTypeDAO.getCourseTypePage(page, size, courseTypeName);

        // 转换为 CourseTypeInfoDTO
        List<CourseTypeInfoDTO> courseTypeInfoList = pageResult.getRecords().stream()
                .map(this::convertToCourseTypeInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<CourseTypeInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), courseTypeInfoList);

        log.info("查询课程类型分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateCourseType(String courseTypeUuid, String courseTypeName) {
        log.info("更新课程类型信息 - UUID: {}, 名称: {}", courseTypeUuid, courseTypeName);

        // 查询课程类型是否存在
        CourseTypeDO courseType = courseTypeDAO.getById(courseTypeUuid);
        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + courseTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查课程类型名称是否被其他课程类型使用
        if (courseTypeDAO.existsByCourseTypeNameExcludeUuid(courseTypeName, courseTypeUuid)) {
            throw new BusinessException("课程类型名称已被其他课程类型使用: " + courseTypeName, ErrorCode.OPERATION_FAILED);
        }

        // 更新课程类型信息
        courseType.setTypeName(courseTypeName);

        // 保存更新
        boolean updated = courseTypeDAO.updateById(courseType);

        if (!updated) {
            throw new BusinessException("更新课程类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程类型更新成功 - UUID: {}, 名称: {}", courseTypeUuid, courseTypeName);
    }

    @Override
    public CourseTypeInfoDTO getCourseType(String courseTypeUuid) {
        log.info("获取课程类型信息 - UUID: {}", courseTypeUuid);

        // 查询课程类型
        CourseTypeDO courseType = courseTypeDAO.getById(courseTypeUuid);

        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + courseTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        CourseTypeInfoDTO courseTypeInfoDTO = convertToCourseTypeInfoDTO(courseType);

        log.info("获取课程类型信息成功 - UUID: {}, 名称: {}",
                courseTypeUuid, courseType.getTypeName());

        return courseTypeInfoDTO;
    }

    @Override
    public void deleteCourseType(String courseTypeUuid) {
        log.info("删除课程类型 - UUID: {}", courseTypeUuid);

        // 查询课程类型是否存在
        CourseTypeDO courseType = courseTypeDAO.getById(courseTypeUuid);
        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + courseTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = courseTypeDAO.removeById(courseTypeUuid);

        if (!deleted) {
            throw new BusinessException("删除课程类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程类型删除成功 - UUID: {}, 名称: {}", courseTypeUuid, courseType.getTypeName());
    }

    /**
     * 转换 CourseTypeDO 为 CourseTypeInfoDTO
     *
     * @param courseTypeDO 课程类型实体
     * @return 课程类型信息DTO
     */
    private CourseTypeInfoDTO convertToCourseTypeInfoDTO(CourseTypeDO courseTypeDO) {
        CourseTypeInfoDTO dto = new CourseTypeInfoDTO();
        dto.setCourseTypeUuid(courseTypeDO.getCourseTypeUuid());
        dto.setTypeName(courseTypeDO.getTypeName());
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
    private PageDTO<CourseTypeInfoDTO> buildPageDTO(int page, int size, int total, List<CourseTypeInfoDTO> records) {
        PageDTO<CourseTypeInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
