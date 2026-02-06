package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.MajorDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.MajorInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.MajorDO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassService;
import io.github.flashlack1314.smartschedulecorev2.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 行政班级服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final ClassDAO classDAO;
    private final MajorDAO majorDAO;
    private final StudentDAO studentDAO;
    private final MajorService majorService;

    @Override
    public void addClass(String majorUuid, String className) {
        log.info("添加行政班级 - 专业UUID: {}, 班级名称: {}", majorUuid, className);

        // 检查专业是否存在
        MajorDO major = majorDAO.getById(majorUuid);
        if (major == null) {
            throw new BusinessException("专业不存在: " + majorUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查班级名称在同一专业下是否已存在
        if (classDAO.existsByClassNameAndMajorUuid(className, majorUuid)) {
            throw new BusinessException("该专业下班级名称已存在: " + className, ErrorCode.OPERATION_FAILED);
        }

        // 创建班级对象
        ClassDO classDO = new ClassDO();
        classDO.setClassUuid(UuidUtil.generateUuidNoDash());
        classDO.setMajorUuid(majorUuid);
        classDO.setClassName(className);

        // 保存到数据库
        boolean saved = classDAO.save(classDO);

        if (!saved) {
            throw new BusinessException("保存行政班级失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("行政班级添加成功 - UUID: {}, 名称: {}", classDO.getClassUuid(), className);
    }

    @Override
    public PageDTO<ClassInfoDTO> getClassPage(int page, int size, String className, String majorUuid, String departmentUuid) {
        log.info("查询行政班级分页信息 - page: {}, size: {}, className: {}, majorUuid: {}, departmentUuid: {}",
                page, size, className, majorUuid, departmentUuid);

        // 调用DAO层进行分页查询
        IPage<ClassDO> pageResult = classDAO.getClassPage(page, size, className, majorUuid, departmentUuid);

        // 转换为 ClassInfoDTO
        List<ClassInfoDTO> classInfoList = pageResult.getRecords().stream()
                .map(this::convertToClassInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<ClassInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), classInfoList);

        log.info("查询行政班级分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateClass(String classUuid, String majorUuid, String className) {
        log.info("更新行政班级信息 - UUID: {}, 专业UUID: {}, 班级名称: {}",
                classUuid, majorUuid, className);

        // 查询班级是否存在
        ClassDO classDO = classDAO.getById(classUuid);
        if (classDO == null) {
            throw new BusinessException("行政班级不存在: " + classUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查专业是否存在
        MajorDO major = majorDAO.getById(majorUuid);
        if (major == null) {
            throw new BusinessException("专业不存在: " + majorUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查班级名称在同一专业下是否被其他班级使用
        if (classDAO.existsByClassNameAndMajorUuidExcludeUuid(className, majorUuid, classUuid)) {
            throw new BusinessException("该专业下班级名称已被其他班级使用: " + className, ErrorCode.OPERATION_FAILED);
        }

        // 更新班级信息
        classDO.setMajorUuid(majorUuid);
        classDO.setClassName(className);

        // 保存更新
        boolean updated = classDAO.updateById(classDO);

        if (!updated) {
            throw new BusinessException("更新行政班级失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("行政班级更新成功 - UUID: {}, 名称: {}", classUuid, className);
    }

    @Override
    public ClassInfoDTO getClass(String classUuid) {
        log.info("获取行政班级信息 - UUID: {}", classUuid);

        // 查询班级
        ClassDO classDO = classDAO.getById(classUuid);

        if (classDO == null) {
            throw new BusinessException("行政班级不存在: " + classUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        ClassInfoDTO classInfoDTO = convertToClassInfoDTO(classDO);

        log.info("获取行政班级信息成功 - UUID: {}, 名称: {}",
                classUuid, classDO.getClassName());

        return classInfoDTO;
    }

    @Override
    public void deleteClass(String classUuid) {
        log.info("删除行政班级 - UUID: {}", classUuid);

        // 查询班级是否存在
        ClassDO classDO = classDAO.getById(classUuid);
        if (classDO == null) {
            throw new BusinessException("行政班级不存在: " + classUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查班级下是否还有学生
        if (studentDAO.existsByClassUuid(classUuid)) {
            long studentCount = studentDAO.countByClassUuid(classUuid);
            throw new BusinessException("该班级下还有 " + studentCount + " 个学生，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = classDAO.removeById(classUuid);

        if (!deleted) {
            throw new BusinessException("删除行政班级失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("行政班级删除成功 - UUID: {}, 名称: {}",
                classUuid, classDO.getClassName());
    }

    /**
     * 转换 ClassDO 为 ClassInfoDTO
     *
     * @param classDO 班级实体
     * @return 班级信息DTO
     */
    private ClassInfoDTO convertToClassInfoDTO(ClassDO classDO) {
        ClassInfoDTO dto = new ClassInfoDTO();
        dto.setClassUuid(classDO.getClassUuid());
        dto.setClassName(classDO.getClassName());

        // 获取专业信息（包含学院信息）
        MajorInfoDTO majorInfo = majorService.getMajor(classDO.getMajorUuid());
        dto.setMajorInfo(majorInfo);

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
    private PageDTO<ClassInfoDTO> buildPageDTO(int page, int size, int total, List<ClassInfoDTO> records) {
        PageDTO<ClassInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
