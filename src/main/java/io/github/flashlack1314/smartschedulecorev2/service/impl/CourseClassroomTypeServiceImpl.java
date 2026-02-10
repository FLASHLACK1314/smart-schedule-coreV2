package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.CourseTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseClassroomTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseClassroomTypeDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseTypeDO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseClassroomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程类型-教室类型关联服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseClassroomTypeServiceImpl implements CourseClassroomTypeService {
    private final CourseClassroomTypeDAO courseClassroomTypeDAO;
    private final CourseTypeDAO courseTypeDAO;
    private final ClassroomTypeDAO classroomTypeDAO;

    @Override
    public void addRelation(String courseTypeUuid, String classroomTypeUuid) {
        log.info("添加课程类型-教室类型关联 - 课程类型UUID: {}, 教室类型UUID: {}",
                courseTypeUuid, classroomTypeUuid);

        // 验证课程类型是否存在
        CourseTypeDO courseType = courseTypeDAO.getById(courseTypeUuid);
        if (courseType == null) {
            throw new BusinessException("课程类型不存在: " + courseTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 验证教室类型是否存在
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroomTypeUuid);
        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroomTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查关联是否已存在
        if (courseClassroomTypeDAO.existsByCourseTypeUuidAndClassroomTypeUuid(courseTypeUuid, classroomTypeUuid)) {
            throw new BusinessException("关联已存在", ErrorCode.OPERATION_FAILED);
        }

        // 创建关联对象
        CourseClassroomTypeDO relation = new CourseClassroomTypeDO();
        relation.setRelationUuid(UuidUtil.generateUuidNoDash());
        relation.setCourseTypeUuid(courseTypeUuid);
        relation.setClassroomTypeUuid(classroomTypeUuid);

        // 保存到数据库
        boolean saved = courseClassroomTypeDAO.save(relation);

        if (!saved) {
            throw new BusinessException("保存关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程类型-教室类型关联添加成功 - 关联UUID: {}, 课程类型: {}, 教室类型: {}",
                relation.getRelationUuid(), courseType.getTypeName(), classroomType.getTypeName());
    }

    @Override
    public PageDTO<CourseClassroomTypeInfoDTO> getRelationPage(int page, int size,
                                                               String courseTypeUuid, String classroomTypeUuid) {
        log.info("查询课程类型-教室类型关联分页信息 - page: {}, size: {}, courseTypeUuid: {}, classroomTypeUuid: {}",
                page, size, courseTypeUuid, classroomTypeUuid);

        // 调用DAO层进行分页查询
        IPage<CourseClassroomTypeDO> pageResult = courseClassroomTypeDAO.getRelationPage(
                page, size, courseTypeUuid, classroomTypeUuid);

        // 转换为 CourseClassroomTypeInfoDTO
        List<CourseClassroomTypeInfoDTO> relationInfoList = pageResult.getRecords().stream()
                .map(this::convertToRelationInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<CourseClassroomTypeInfoDTO> result = buildPageDTO(page, size,
                (int) pageResult.getTotal(), relationInfoList);

        log.info("查询课程类型-教室类型关联分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void deleteRelation(String relationUuid) {
        log.info("删除课程类型-教室类型关联 - 关联UUID: {}", relationUuid);

        // 查询关联是否存在
        CourseClassroomTypeDO relation = courseClassroomTypeDAO.getById(relationUuid);
        if (relation == null) {
            throw new BusinessException("关联不存在: " + relationUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = courseClassroomTypeDAO.removeById(relationUuid);

        if (!deleted) {
            throw new BusinessException("删除关联失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("课程类型-教室类型关联删除成功 - 关联UUID: {}", relationUuid);
    }

    /**
     * 转换 CourseClassroomTypeDO 为 CourseClassroomTypeInfoDTO
     *
     * @param relation 关联实体
     * @return 关联信息DTO
     */
    private CourseClassroomTypeInfoDTO convertToRelationInfoDTO(CourseClassroomTypeDO relation) {
        CourseClassroomTypeInfoDTO dto = new CourseClassroomTypeInfoDTO();
        dto.setRelationUuid(relation.getRelationUuid());
        dto.setCourseTypeUuid(relation.getCourseTypeUuid());
        dto.setClassroomTypeUuid(relation.getClassroomTypeUuid());

        // 查询并填充课程类型名称
        CourseTypeDO courseType = courseTypeDAO.getById(relation.getCourseTypeUuid());
        if (courseType != null) {
            dto.setCourseTypeName(courseType.getTypeName());
        }

        // 查询并填充教室类型名称
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(relation.getClassroomTypeUuid());
        if (classroomType != null) {
            dto.setClassroomTypeName(classroomType.getTypeName());
        }

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
    private PageDTO<CourseClassroomTypeInfoDTO> buildPageDTO(int page, int size, int total,
                                                             List<CourseClassroomTypeInfoDTO> records) {
        PageDTO<CourseClassroomTypeInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
