package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.MajorDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.MajorInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.MajorDO;
import io.github.flashlack1314.smartschedulecorev2.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {
    private final MajorDAO majorDAO;
    private final DepartmentDAO departmentDAO;
    private final ClassDAO classDAO;

    @Override
    public void addMajor(String departmentUuid, String majorNum, String majorName) {
        log.info("添加专业 - 学院UUID: {}, 编号: {}, 名称: {}", departmentUuid, majorNum, majorName);

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查专业编号是否已存在
        if (majorDAO.existsByMajorNum(majorNum)) {
            throw new BusinessException("专业编号已存在: " + majorNum, ErrorCode.OPERATION_FAILED);
        }

        // 创建专业对象
        MajorDO majorDO = new MajorDO();
        majorDO.setMajorUuid(UuidUtil.generateUuidNoDash());
        majorDO.setDepartmentUuid(departmentUuid);
        majorDO.setMajorNum(majorNum);
        majorDO.setMajorName(majorName);

        // 保存到数据库
        boolean saved = majorDAO.save(majorDO);

        if (!saved) {
            throw new BusinessException("保存专业失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("专业添加成功 - UUID: {}, 编号: {}, 名称: {}",
                majorDO.getMajorUuid(), majorNum, majorName);
    }

    @Override
    public PageDTO<MajorInfoDTO> getMajorPage(int page, int size, String departmentUuid, String majorNum, String majorName) {
        log.info("查询专业分页信息 - page: {}, size: {}, departmentUuid: {}, majorNum: {}, majorName: {}",
                page, size, departmentUuid, majorNum, majorName);

        // 调用DAO层进行分页查询
        IPage<MajorDO> pageResult = majorDAO.getMajorPage(page, size, departmentUuid, majorNum, majorName);

        // 转换为 MajorInfoDTO
        List<MajorInfoDTO> majorInfoList = pageResult.getRecords().stream()
                .map(this::convertToMajorInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<MajorInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), majorInfoList);

        log.info("查询专业分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateMajor(String majorUuid, String departmentUuid, String majorNum, String majorName) {
        log.info("更新专业信息 - UUID: {}, 学院UUID: {}, 编号: {}, 名称: {}",
                majorUuid, departmentUuid, majorNum, majorName);

        // 查询专业是否存在
        MajorDO major = majorDAO.getById(majorUuid);
        if (major == null) {
            throw new BusinessException("专业不存在: " + majorUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查专业编号是否被其他专业使用
        if (majorDAO.existsByMajorNumExcludeUuid(majorNum, majorUuid)) {
            throw new BusinessException("专业编号已被其他专业使用: " + majorNum, ErrorCode.OPERATION_FAILED);
        }

        // 更新专业信息
        major.setDepartmentUuid(departmentUuid);
        major.setMajorNum(majorNum);
        major.setMajorName(majorName);

        // 保存更新
        boolean updated = majorDAO.updateById(major);

        if (!updated) {
            throw new BusinessException("更新专业失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("专业更新成功 - UUID: {}, 编号: {}, 名称: {}", majorUuid, majorNum, majorName);
    }

    @Override
    public MajorInfoDTO getMajor(String majorUuid) {
        log.info("获取专业信息 - UUID: {}", majorUuid);

        // 查询专业
        MajorDO major = majorDAO.getById(majorUuid);

        if (major == null) {
            throw new BusinessException("专业不存在: " + majorUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        MajorInfoDTO majorInfoDTO = convertToMajorInfoDTO(major);

        log.info("获取专业信息成功 - UUID: {}, 编号: {}, 名称: {}",
                majorUuid, major.getMajorNum(), major.getMajorName());

        return majorInfoDTO;
    }

    @Override
    public void deleteMajor(String majorUuid) {
        log.info("删除专业 - UUID: {}", majorUuid);

        // 查询专业是否存在
        MajorDO major = majorDAO.getById(majorUuid);
        if (major == null) {
            throw new BusinessException("专业不存在: " + majorUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查专业下是否还有班级
        if (classDAO.existsByMajorUuid(majorUuid)) {
            long classCount = classDAO.countByMajorUuid(majorUuid);
            throw new BusinessException("该专业下还有 " + classCount + " 个班级，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = majorDAO.removeById(majorUuid);

        if (!deleted) {
            throw new BusinessException("删除专业失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("专业删除成功 - UUID: {}, 编号: {}, 名称: {}",
                majorUuid, major.getMajorNum(), major.getMajorName());
    }

    /**
     * 转换 MajorDO 为 MajorInfoDTO
     *
     * @param majorDO 专业实体
     * @return 专业信息DTO
     */
    private MajorInfoDTO convertToMajorInfoDTO(MajorDO majorDO) {
        MajorInfoDTO dto = new MajorInfoDTO();
        dto.setMajorUuid(majorDO.getMajorUuid());
        dto.setDepartmentUuid(majorDO.getDepartmentUuid());
        dto.setMajorNum(majorDO.getMajorNum());
        dto.setMajorName(majorDO.getMajorName());

        // 获取学院名称
        DepartmentDO department = departmentDAO.getById(majorDO.getDepartmentUuid());
        if (department != null) {
            dto.setDepartmentName(department.getDepartmentName());
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
    private PageDTO<MajorInfoDTO> buildPageDTO(int page, int size, int total, List<MajorInfoDTO> records) {
        PageDTO<MajorInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
