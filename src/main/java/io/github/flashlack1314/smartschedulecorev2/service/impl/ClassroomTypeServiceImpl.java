package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassroomTypeInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教室类型服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomTypeServiceImpl implements ClassroomTypeService {
    private final ClassroomTypeDAO classroomTypeDAO;

    @Override
    public void addClassroomType(String classroomTypeName) {
        log.info("添加教室类型 - 名称: {}", classroomTypeName);

        // 检查教室类型名称是否已存在
        if (classroomTypeDAO.existsByClassroomTypeName(classroomTypeName)) {
            throw new BusinessException("教室类型名称已存在: " + classroomTypeName, ErrorCode.OPERATION_FAILED);
        }

        // 创建教室类型对象
        ClassroomTypeDO classroomTypeDO = new ClassroomTypeDO();
        classroomTypeDO.setClassroomTypeUuid(UuidUtil.generateUuidNoDash());
        classroomTypeDO.setTypeName(classroomTypeName);

        // 保存到数据库
        boolean saved = classroomTypeDAO.save(classroomTypeDO);

        if (!saved) {
            throw new BusinessException("保存教室类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室类型添加成功 - UUID: {}, 名称: {}", classroomTypeDO.getClassroomTypeUuid(), classroomTypeName);
    }

    @Override
    public PageDTO<ClassroomTypeInfoDTO> getClassroomTypePage(int page, int size, String classroomTypeName) {
        log.info("查询教室类型分页信息 - page: {}, size: {}, classroomTypeName: {}",
                page, size, classroomTypeName);

        // 调用DAO层进行分页查询
        IPage<ClassroomTypeDO> pageResult = classroomTypeDAO.getClassroomTypePage(page, size, classroomTypeName);

        // 转换为 ClassroomTypeInfoDTO
        List<ClassroomTypeInfoDTO> classroomTypeInfoList = pageResult.getRecords().stream()
                .map(this::convertToClassroomTypeInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<ClassroomTypeInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), classroomTypeInfoList);

        log.info("查询教室类型分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateClassroomType(String classroomTypeUuid, String classroomTypeName) {
        log.info("更新教室类型信息 - UUID: {}, 名称: {}", classroomTypeUuid, classroomTypeName);

        // 查询教室类型是否存在
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroomTypeUuid);
        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroomTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教室类型名称是否被其他教室类型使用
        if (classroomTypeDAO.existsByClassroomTypeNameExcludeUuid(classroomTypeName, classroomTypeUuid)) {
            throw new BusinessException("教室类型名称已被其他教室类型使用: " + classroomTypeName, ErrorCode.OPERATION_FAILED);
        }

        // 更新教室类型信息
        classroomType.setTypeName(classroomTypeName);

        // 保存更新
        boolean updated = classroomTypeDAO.updateById(classroomType);

        if (!updated) {
            throw new BusinessException("更新教室类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室类型更新成功 - UUID: {}, 名称: {}", classroomTypeUuid, classroomTypeName);
    }

    @Override
    public ClassroomTypeInfoDTO getClassroomType(String classroomTypeUuid) {
        log.info("获取教室类型信息 - UUID: {}", classroomTypeUuid);

        // 查询教室类型
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroomTypeUuid);

        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroomTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        ClassroomTypeInfoDTO classroomTypeInfoDTO = convertToClassroomTypeInfoDTO(classroomType);

        log.info("获取教室类型信息成功 - UUID: {}, 名称: {}",
                classroomTypeUuid, classroomType.getTypeName());

        return classroomTypeInfoDTO;
    }

    @Override
    public void deleteClassroomType(String classroomTypeUuid) {
        log.info("删除教室类型 - UUID: {}", classroomTypeUuid);

        // 查询教室类型是否存在
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroomTypeUuid);
        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroomTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = classroomTypeDAO.removeById(classroomTypeUuid);

        if (!deleted) {
            throw new BusinessException("删除教室类型失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室类型删除成功 - UUID: {}, 名称: {}", classroomTypeUuid, classroomType.getTypeName());
    }

    /**
     * 转换 ClassroomTypeDO 为 ClassroomTypeInfoDTO
     *
     * @param classroomTypeDO 教室类型实体
     * @return 教室类型信息DTO
     */
    private ClassroomTypeInfoDTO convertToClassroomTypeInfoDTO(ClassroomTypeDO classroomTypeDO) {
        ClassroomTypeInfoDTO dto = new ClassroomTypeInfoDTO();
        dto.setClassroomTypeUuid(classroomTypeDO.getClassroomTypeUuid());
        dto.setTypeName(classroomTypeDO.getTypeName());
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
    private PageDTO<ClassroomTypeInfoDTO> buildPageDTO(int page, int size, int total, List<ClassroomTypeInfoDTO> records) {
        PageDTO<ClassroomTypeInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
