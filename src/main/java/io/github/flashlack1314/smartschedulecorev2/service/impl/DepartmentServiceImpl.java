package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.MajorDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.DepartmentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentDAO departmentDAO;
    private final MajorDAO majorDAO;

    @Override
    public void addDepartment(String departmentName) {
        log.info("添加学院 - 名称: {}", departmentName);

        // 检查学院名称是否已存在
        if (departmentDAO.existsByDepartmentName(departmentName)) {
            throw new BusinessException("学院名称已存在: " + departmentName, ErrorCode.OPERATION_FAILED);
        }

        // 创建学院对象
        DepartmentDO departmentDO = new DepartmentDO();
        departmentDO.setDepartmentUuid(UuidUtil.generateUuidNoDash());
        departmentDO.setDepartmentName(departmentName);

        // 保存到数据库
        boolean saved = departmentDAO.save(departmentDO);

        if (!saved) {
            throw new BusinessException("保存学院失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学院添加成功 - UUID: {}, 名称: {}", departmentDO.getDepartmentUuid(), departmentName);
    }

    @Override
    public PageDTO<DepartmentInfoDTO> getDepartmentPage(int page, int size, String departmentName) {
        log.info("查询学院分页信息 - page: {}, size: {}, departmentName: {}",
                page, size, departmentName);

        // 调用DAO层进行分页查询
        IPage<DepartmentDO> pageResult = departmentDAO.getDepartmentPage(page, size, departmentName);

        // 转换为 DepartmentInfoDTO
        List<DepartmentInfoDTO> departmentInfoList = pageResult.getRecords().stream()
                .map(this::convertToDepartmentInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<DepartmentInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), departmentInfoList);

        log.info("查询学院分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateDepartment(String departmentUuid, String departmentName) {
        log.info("更新学院信息 - UUID: {}, 名称: {}", departmentUuid, departmentName);

        // 查询学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学院名称是否被其他学院使用
        if (departmentDAO.existsByDepartmentNameExcludeUuid(departmentName, departmentUuid)) {
            throw new BusinessException("学院名称已被其他学院使用: " + departmentName, ErrorCode.OPERATION_FAILED);
        }

        // 更新学院信息
        department.setDepartmentName(departmentName);

        // 保存更新
        boolean updated = departmentDAO.updateById(department);

        if (!updated) {
            throw new BusinessException("更新学院失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学院更新成功 - UUID: {}, 名称: {}", departmentUuid, departmentName);
    }

    @Override
    public DepartmentInfoDTO getDepartment(String departmentUuid) {
        log.info("获取学院信息 - UUID: {}", departmentUuid);

        // 查询学院
        DepartmentDO department = departmentDAO.getById(departmentUuid);

        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        DepartmentInfoDTO departmentInfoDTO = convertToDepartmentInfoDTO(department);

        log.info("获取学院信息成功 - UUID: {}, 名称: {}",
                departmentUuid, department.getDepartmentName());

        return departmentInfoDTO;
    }

    @Override
    public void deleteDepartment(String departmentUuid) {
        log.info("删除学院 - UUID: {}", departmentUuid);

        // 查询学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学院下是否还有专业
        if (majorDAO.existsByDepartmentUuid(departmentUuid)) {
            long majorCount = majorDAO.countByDepartmentUuid(departmentUuid);
            throw new BusinessException("该学院下还有 " + majorCount + " 个专业，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = departmentDAO.removeById(departmentUuid);

        if (!deleted) {
            throw new BusinessException("删除学院失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学院删除成功 - UUID: {}, 名称: {}", departmentUuid, department.getDepartmentName());
    }

    /**
     * 转换 DepartmentDO 为 DepartmentInfoDTO
     *
     * @param departmentDO 学院实体
     * @return 学院信息DTO
     */
    private DepartmentInfoDTO convertToDepartmentInfoDTO(DepartmentDO departmentDO) {
        DepartmentInfoDTO dto = new DepartmentInfoDTO();
        dto.setDepartmentUuid(departmentDO.getDepartmentUuid());
        dto.setDepartmentName(departmentDO.getDepartmentName());
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
    private PageDTO<DepartmentInfoDTO> buildPageDTO(int page, int size, int total, List<DepartmentInfoDTO> records) {
        PageDTO<DepartmentInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
