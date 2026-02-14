package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.SemesterDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SemesterInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SemesterDO;
import io.github.flashlack1314.smartschedulecorev2.service.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学期服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {
    private final SemesterDAO semesterDAO;

    @Override
    public void addSemester(String semesterName, Integer semesterWeeks) {
        log.info("添加学期 - 名称: {}, 周数: {}", semesterName, semesterWeeks);

        // 检查学期名称是否已存在
        if (semesterDAO.existsBySemesterName(semesterName)) {
            throw new BusinessException("学期名称已存在: " + semesterName, ErrorCode.OPERATION_FAILED);
        }

        // 创建学期对象
        SemesterDO semesterDO = new SemesterDO();
        semesterDO.setSemesterUuid(UuidUtil.generateUuidNoDash());
        semesterDO.setSemesterName(semesterName);
        semesterDO.setSemesterWeeks(semesterWeeks);

        // 保存到数据库
        boolean saved = semesterDAO.save(semesterDO);

        if (!saved) {
            throw new BusinessException("保存学期失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学期添加成功 - UUID: {}, 名称: {}, 周数: {}", semesterDO.getSemesterUuid(), semesterName, semesterWeeks);
    }

    @Override
    public PageDTO<SemesterInfoDTO> getSemesterPage(int page, int size, String semesterName) {
        log.info("查询学期分页信息 - page: {}, size: {}, semesterName: {}",
                page, size, semesterName);

        // 调用DAO层进行分页查询
        IPage<SemesterDO> pageResult = semesterDAO.getSemesterPage(page, size, semesterName);

        // 转换为 SemesterInfoDTO
        List<SemesterInfoDTO> semesterInfoList = pageResult.getRecords().stream()
                .map(this::convertToSemesterInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<SemesterInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), semesterInfoList);

        log.info("查询学期分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateSemester(String semesterUuid, String semesterName, Integer semesterWeeks) {
        log.info("更新学期信息 - UUID: {}, 名称: {}, 周数: {}", semesterUuid, semesterName, semesterWeeks);

        // 查询学期是否存在
        SemesterDO semester = semesterDAO.getById(semesterUuid);
        if (semester == null) {
            throw new BusinessException("学期不存在: " + semesterUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学期名称是否被其他学期使用
        if (semesterDAO.existsBySemesterNameExcludeUuid(semesterName, semesterUuid)) {
            throw new BusinessException("学期名称已被其他学期使用: " + semesterName, ErrorCode.OPERATION_FAILED);
        }

        // 更新学期信息
        semester.setSemesterName(semesterName);
        semester.setSemesterWeeks(semesterWeeks);

        // 保存更新
        boolean updated = semesterDAO.updateById(semester);

        if (!updated) {
            throw new BusinessException("更新学期失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学期更新成功 - UUID: {}, 名称: {}, 周数: {}", semesterUuid, semesterName, semesterWeeks);
    }

    @Override
    public SemesterInfoDTO getSemester(String semesterUuid) {
        log.info("获取学期信息 - UUID: {}", semesterUuid);

        // 查询学期
        SemesterDO semester = semesterDAO.getById(semesterUuid);

        if (semester == null) {
            throw new BusinessException("学期不存在: " + semesterUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        SemesterInfoDTO semesterInfoDTO = convertToSemesterInfoDTO(semester);

        log.info("获取学期信息成功 - UUID: {}, 名称: {}",
                semesterUuid, semester.getSemesterName());

        return semesterInfoDTO;
    }

    @Override
    public void deleteSemester(String semesterUuid) {
        log.info("删除学期 - UUID: {}", semesterUuid);

        // 查询学期是否存在
        SemesterDO semester = semesterDAO.getById(semesterUuid);
        if (semester == null) {
            throw new BusinessException("学期不存在: " + semesterUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = semesterDAO.removeById(semesterUuid);

        if (!deleted) {
            throw new BusinessException("删除学期失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学期删除成功 - UUID: {}, 名称: {}", semesterUuid, semester.getSemesterName());
    }

    /**
     * 转换 SemesterDO 为 SemesterInfoDTO
     *
     * @param semesterDO 学期实体
     * @return 学期信息DTO
     */
    private SemesterInfoDTO convertToSemesterInfoDTO(SemesterDO semesterDO) {
        SemesterInfoDTO dto = new SemesterInfoDTO();
        dto.setSemesterUuid(semesterDO.getSemesterUuid());
        dto.setSemesterName(semesterDO.getSemesterName());
        dto.setSemesterWeeks(semesterDO.getSemesterWeeks());
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
    private PageDTO<SemesterInfoDTO> buildPageDTO(int page, int size, int total, List<SemesterInfoDTO> records) {
        PageDTO<SemesterInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
