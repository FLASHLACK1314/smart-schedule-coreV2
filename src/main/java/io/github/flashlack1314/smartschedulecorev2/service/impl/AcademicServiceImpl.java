package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.AcademicAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.DepartmentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.service.AcademicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教务服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {
    private final AcademicAdminDAO academicAdminDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public void addAcademicAdmin(String academicNum, String academicName, String departmentUuid, String academicPassword) {
        log.info("添加教务管理员 - 工号: {}, 姓名: {}, 学院UUID: {}", academicNum, academicName, departmentUuid);

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教务工号是否已存在
        if (academicAdminDAO.existsByAcademicNum(academicNum)) {
            throw new BusinessException("教务工号已存在: " + academicNum, ErrorCode.OPERATION_FAILED);
        }

        // 创建教务管理员对象
        AcademicAdminDO academicAdminDO = new AcademicAdminDO();
        academicAdminDO.setAcademicUuid(UuidUtil.generateUuidNoDash());
        academicAdminDO.setAcademicNum(academicNum);
        academicAdminDO.setAcademicName(academicName);
        academicAdminDO.setDepartmentUuid(departmentUuid);
        academicAdminDO.setAcademicPassword(PasswordUtil.encrypt(academicPassword));

        // 保存到数据库
        boolean saved = academicAdminDAO.save(academicAdminDO);

        if (!saved) {
            throw new BusinessException("保存教务管理员失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教务管理员添加成功 - UUID: {}, 工号: {}, 姓名: {}",
                academicAdminDO.getAcademicUuid(), academicNum, academicName);
    }

    @Override
    public PageDTO<AcademicAdminInfoDTO> getAcademicAdminPage(int page, int size, String academicName,
                                                               String academicNum, String departmentUuid) {
        log.info("查询教务管理员分页信息 - page: {}, size: {}, academicName: {}, academicNum: {}, departmentUuid: {}",
                page, size, academicName, academicNum, departmentUuid);

        // 调用DAO层进行分页查询
        IPage<AcademicAdminDO> pageResult = academicAdminDAO.getAcademicAdminPage(page, size, academicName, academicNum, departmentUuid);

        // 转换为 AcademicAdminInfoDTO
        List<AcademicAdminInfoDTO> academicAdminInfoList = pageResult.getRecords().stream()
                .map(this::convertToAcademicAdminInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<AcademicAdminInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), academicAdminInfoList);

        log.info("查询教务管理员分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateAcademicAdmin(String academicUuid, String academicNum, String academicName,
                                     String departmentUuid, String academicPassword) {
        log.info("更新教务管理员信息 - UUID: {}, 工号: {}, 姓名: {}",
                academicUuid, academicNum, academicName);

        // 查询教务是否存在
        AcademicAdminDO academicAdmin = academicAdminDAO.getById(academicUuid);
        if (academicAdmin == null) {
            throw new BusinessException("教务管理员不存在: " + academicUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教务工号是否被其他教务使用
        if (academicAdminDAO.existsByAcademicNumExcludeUuid(academicNum, academicUuid)) {
            throw new BusinessException("教务工号已被其他教务使用: " + academicNum, ErrorCode.OPERATION_FAILED);
        }

        // 更新教务管理员信息
        academicAdmin.setAcademicNum(academicNum);
        academicAdmin.setAcademicName(academicName);
        academicAdmin.setDepartmentUuid(departmentUuid);

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(academicPassword)) {
            academicAdmin.setAcademicPassword(PasswordUtil.encrypt(academicPassword));
        }

        // 保存更新
        boolean updated = academicAdminDAO.updateById(academicAdmin);

        if (!updated) {
            throw new BusinessException("更新教务管理员失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教务管理员更新成功 - UUID: {}, 工号: {}, 姓名: {}", academicUuid, academicNum, academicName);
    }

    @Override
    public AcademicAdminInfoDTO getAcademicAdmin(String academicUuid) {
        log.info("获取教务管理员信息 - UUID: {}", academicUuid);

        // 查询教务管理员
        AcademicAdminDO academicAdmin = academicAdminDAO.getById(academicUuid);

        if (academicAdmin == null) {
            throw new BusinessException("教务管理员不存在: " + academicUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        AcademicAdminInfoDTO academicAdminInfoDTO = convertToAcademicAdminInfoDTO(academicAdmin);

        log.info("获取教务管理员信息成功 - UUID: {}, 工号: {}, 姓名: {}",
                academicUuid, academicAdmin.getAcademicNum(), academicAdmin.getAcademicName());

        return academicAdminInfoDTO;
    }

    @Override
    public void deleteAcademicAdmin(String academicUuid) {
        log.info("删除教务管理员 - UUID: {}", academicUuid);

        // 查询教务是否存在
        AcademicAdminDO academicAdmin = academicAdminDAO.getById(academicUuid);
        if (academicAdmin == null) {
            throw new BusinessException("教务管理员不存在: " + academicUuid, ErrorCode.OPERATION_FAILED);
        }

        // 教务管理员无外键引用，可直接删除

        // 执行删除
        boolean deleted = academicAdminDAO.removeById(academicUuid);

        if (!deleted) {
            throw new BusinessException("删除教务管理员失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教务管理员删除成功 - UUID: {}, 工号: {}, 姓名: {}",
                academicUuid, academicAdmin.getAcademicNum(), academicAdmin.getAcademicName());
    }

    /**
     * 转换 AcademicAdminDO 为 AcademicAdminInfoDTO
     *
     * @param academicAdminDO 教务管理员实体
     * @return 教务管理员信息DTO
     */
    private AcademicAdminInfoDTO convertToAcademicAdminInfoDTO(AcademicAdminDO academicAdminDO) {
        AcademicAdminInfoDTO dto = new AcademicAdminInfoDTO();
        dto.setAcademicUuid(academicAdminDO.getAcademicUuid());
        dto.setAcademicNum(academicAdminDO.getAcademicNum());
        dto.setAcademicName(academicAdminDO.getAcademicName());

        // 获取学院信息
        DepartmentDO department = departmentDAO.getById(academicAdminDO.getDepartmentUuid());
        if (department != null) {
            DepartmentInfoDTO departmentInfo = new DepartmentInfoDTO();
            departmentInfo.setDepartmentUuid(department.getDepartmentUuid());
            departmentInfo.setDepartmentName(department.getDepartmentName());
            dto.setDepartmentInfo(departmentInfo);
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
    private PageDTO<AcademicAdminInfoDTO> buildPageDTO(int page, int size, int total, List<AcademicAdminInfoDTO> records) {
        PageDTO<AcademicAdminInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
