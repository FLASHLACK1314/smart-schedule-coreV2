package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.BuildingDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ScheduleDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassroomInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.BuildingDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassroomVO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教室服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {
    private final ClassroomDAO classroomDAO;
    private final BuildingDAO buildingDAO;
    private final ClassroomTypeDAO classroomTypeDAO;
    private final ScheduleDAO scheduleDAO;

    /**
     * 添加教室信息
     *
     * @param getData 包含教室信息的数据传输对象，包括教学楼UUID、教室名称、容量和教室类型UUID
     * @throws BusinessException 当教学楼不存在、教室类型不存在或保存失败时抛出业务异常
     */
    @Override
    public void addClassroom(AddClassroomVO getData) {
        log.info("添加教室 - 教学楼UUID: {}, 教室名称: {}, 容量: {}, 类型UUID: {}",
                getData.getBuildingUuid(), getData.getClassroomName(),
                getData.getCapacity(), getData.getClassroomTypeUuid());

        // 验证关联数据存在性
        if (!buildingDAO.existsByUuid(getData.getBuildingUuid())) {
            throw new BusinessException("教学楼不存在: " + getData.getBuildingUuid(), ErrorCode.OPERATION_FAILED);
        }

        if (!classroomTypeDAO.existsByUuid(getData.getClassroomTypeUuid())) {
            throw new BusinessException("教室类型不存在: " + getData.getClassroomTypeUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 构建教室实体对象
        ClassroomDO classroomDO = new ClassroomDO();
        classroomDO.setClassroomUuid(UuidUtil.generateUuidNoDash());
        classroomDO.setBuildingUuid(getData.getBuildingUuid());
        classroomDO.setClassroomName(getData.getClassroomName());
        classroomDO.setClassroomCapacity(getData.getCapacity());
        classroomDO.setClassroomTypeUuid(getData.getClassroomTypeUuid());

        // 执行数据库保存操作
        boolean saved = classroomDAO.save(classroomDO);

        if (!saved) {
            throw new BusinessException("保存教室失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室添加成功 - UUID: {}, 教学楼UUID: {}, 教室名称: {}",
                classroomDO.getClassroomUuid(), getData.getBuildingUuid(), getData.getClassroomName());
    }


    /**
     * 根据教室UUID获取详细的教室信息
     *
     * @param classroomUuid 教室的唯一标识符
     * @return ClassroomInfoDTO 包含教室详细信息的数据传输对象
     * @throws BusinessException 当教室、教学楼或教室类型不存在时抛出业务异常
     */
    @Override
    public ClassroomInfoDTO getClassroomInfo(String classroomUuid) {
        log.info("获取教室信息 - UUID: {}", classroomUuid);

        // 查询基础教室信息
        ClassroomDO classroom = classroomDAO.getById(classroomUuid);
        if (classroom == null) {
            throw new BusinessException("教室不存在: " + classroomUuid, ErrorCode.OPERATION_FAILED);
        }

        // 获取关联的教学楼信息
        BuildingDO building = buildingDAO.getById(classroom.getBuildingUuid());
        if (building == null) {
            throw new BusinessException("教学楼不存在: " + classroom.getBuildingUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 获取关联的教室类型信息
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroom.getClassroomTypeUuid());
        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroom.getClassroomTypeUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 构建并返回教室信息DTO
        ClassroomInfoDTO classroomInfoDTO = new ClassroomInfoDTO();
        classroomInfoDTO.setClassroomUuid(classroom.getClassroomUuid());
        classroomInfoDTO.setBuildingName(building.getBuildingName());
        classroomInfoDTO.setClassroomName(classroom.getClassroomName());
        classroomInfoDTO.setCapacity(classroom.getClassroomCapacity());
        classroomInfoDTO.setTypeName(classroomType.getTypeName());
        classroomInfoDTO.setTypeDescription(classroomType.getTypeDescription());

        log.info("获取教室信息成功 - UUID: {}, 教学楼: {}, 教室: {}, 类型: {}",
                classroomUuid, building.getBuildingName(), classroom.getClassroomName(), classroomType.getTypeName());

        return classroomInfoDTO;
    }


    /**
     * 分页查询教室列表
     *
     * @param page              页码
     * @param size              每页数量
     * @param buildingUuid      教学楼UUID（可选，用于精确查询）
     * @param classroomName     教室名称（可选，用于模糊查询）
     * @param classroomCapacity 教室容量（可选，用于精确查询）
     * @param classroomTypeUuid 教室类型UUID（可选，用于精确查询）
     * @return 分页结果
     */
    @Override
    public PageDTO<ClassroomInfoDTO> getClassroomPageList(int page, int size, String buildingUuid, String classroomName, String classroomCapacity, String classroomTypeUuid) {
        log.info("查询教室分页信息 - page: {}, size: {}, buildingUuid: {}, classroomName: {}, classroomCapacity: {}, classroomTypeUuid: {}",
                page, size, buildingUuid, classroomName, classroomCapacity, classroomTypeUuid);

        // 调用DAO层进行分页查询
        IPage<ClassroomDO> pageResult = classroomDAO.getClassroomPage(page, size, buildingUuid, classroomName, classroomCapacity, classroomTypeUuid);

        // 转换为 ClassroomInfoDTO
        List<ClassroomInfoDTO> classroomInfoList = pageResult.getRecords().stream()
                .map(this::convertToClassroomInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<ClassroomInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), classroomInfoList);

        log.info("查询教室分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    /**
     * 转换 ClassroomDO 为 ClassroomInfoDTO
     *
     * @param classroomDO 教室实体
     * @return 教室信息DTO
     */
    private ClassroomInfoDTO convertToClassroomInfoDTO(ClassroomDO classroomDO) {
        // 查询教学楼信息
        BuildingDO building = buildingDAO.getById(classroomDO.getBuildingUuid());
        // 查询教室类型信息
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroomDO.getClassroomTypeUuid());

        // 构建 DTO
        ClassroomInfoDTO dto = new ClassroomInfoDTO();
        dto.setClassroomUuid(classroomDO.getClassroomUuid());
        dto.setBuildingName(building != null ? building.getBuildingName() : "未知教学楼");
        dto.setClassroomName(classroomDO.getClassroomName());
        dto.setCapacity(classroomDO.getClassroomCapacity());
        dto.setTypeName(classroomType != null ? classroomType.getTypeName() : "未知类型");
        dto.setTypeDescription(classroomType != null ? classroomType.getTypeDescription() : "");

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
    private PageDTO<ClassroomInfoDTO> buildPageDTO(int page, int size, int total, List<ClassroomInfoDTO> records) {
        PageDTO<ClassroomInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }

    /**
     * 更新教室信息
     *
     * @param classroomUuid     教室UUID
     * @param buildingUuid      教学楼UUID
     * @param classroomName     教室名称
     * @param classroomCapacity 教室容量
     * @param classroomTypeUuid 教室类型UUID
     * @throws BusinessException 当教室、教学楼、教室类型不存在或更新失败时抛出业务异常
     */
    @Override
    public void updateClassroom(String classroomUuid, String buildingUuid, String classroomName, Integer classroomCapacity, String classroomTypeUuid) {
        log.info("更新教室信息 - UUID: {}, 教学楼UUID: {}, 教室名称: {}, 容量: {}, 类型UUID: {}",
                classroomUuid, buildingUuid, classroomName, classroomCapacity, classroomTypeUuid);

        // 查询教室是否存在
        ClassroomDO classroom = classroomDAO.getById(classroomUuid);
        if (classroom == null) {
            throw new BusinessException("教室不存在: " + classroomUuid, ErrorCode.OPERATION_FAILED);
        }

        // 验证教学楼是否存在
        if (!buildingDAO.existsByUuid(buildingUuid)) {
            throw new BusinessException("教学楼不存在: " + buildingUuid, ErrorCode.OPERATION_FAILED);
        }

        // 验证教室类型是否存在
        if (!classroomTypeDAO.existsByUuid(classroomTypeUuid)) {
            throw new BusinessException("教室类型不存在: " + classroomTypeUuid, ErrorCode.OPERATION_FAILED);
        }

        // 更新教室信息
        classroom.setBuildingUuid(buildingUuid);
        classroom.setClassroomName(classroomName);
        classroom.setClassroomCapacity(classroomCapacity);
        classroom.setClassroomTypeUuid(classroomTypeUuid);

        // 保存更新
        boolean updated = classroomDAO.updateById(classroom);
        if (!updated) {
            throw new BusinessException("更新教室失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室更新成功 - UUID: {}, 教学楼UUID: {}, 教室名称: {}, 容量: {}, 类型UUID: {}",
                classroomUuid, buildingUuid, classroomName, classroomCapacity, classroomTypeUuid);
    }

    /**
     * 删除教室
     *
     * @param classroomUuid 教室UUID
     * @throws BusinessException 当教室不存在、教室被排课使用或删除失败时抛出业务异常
     */
    @Override
    public void deleteClassroom(String classroomUuid) {
        log.info("删除教室 - UUID: {}", classroomUuid);

        // 查询教室是否存在
        ClassroomDO classroom = classroomDAO.getById(classroomUuid);
        if (classroom == null) {
            throw new BusinessException("教室不存在: " + classroomUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教室是否被排课使用
        if (scheduleDAO.existsByClassroomUuid(classroomUuid)) {
            long scheduleCount = scheduleDAO.countByClassroomUuid(classroomUuid);
            throw new BusinessException("该教室还有 " + scheduleCount + " 条排课记录，无法删除", ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = classroomDAO.removeById(classroomUuid);
        if (!deleted) {
            throw new BusinessException("删除教室失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室删除成功 - UUID: {}, 教室名称: {}", classroomUuid, classroom.getClassroomName());
    }
}
