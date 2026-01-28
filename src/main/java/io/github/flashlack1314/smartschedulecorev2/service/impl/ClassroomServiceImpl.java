package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.BuildingDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.ClassroomInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.BuildingDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassroomVO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public void addClassroom(AddClassroomVO getData) {
        log.info("添加教室 - 教学楼UUID: {}, 教室名称: {}, 容量: {}, 类型UUID: {}",
                getData.getBuildingUuid(), getData.getClassroomName(),
                getData.getCapacity(), getData.getClassroomTypeUuid());

        // 检查教学楼是否存在
        if (!buildingDAO.existsByUuid(getData.getBuildingUuid())) {
            throw new BusinessException("教学楼不存在: " + getData.getBuildingUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查教室类型是否存在
        if (!classroomTypeDAO.existsByUuid(getData.getClassroomTypeUuid())) {
            throw new BusinessException("教室类型不存在: " + getData.getClassroomTypeUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 创建教室对象
        ClassroomDO classroomDO = new ClassroomDO();
        classroomDO.setClassroomUuid(UuidUtil.generateUuidNoDash());
        classroomDO.setBuildingUuid(getData.getBuildingUuid());
        classroomDO.setClassroomName(getData.getClassroomName());
        classroomDO.setClassroomCapacity(getData.getCapacity());
        classroomDO.setClassroomTypeUuid(getData.getClassroomTypeUuid());

        // 保存到数据库
        boolean saved = classroomDAO.save(classroomDO);

        if (!saved) {
            throw new BusinessException("保存教室失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教室添加成功 - UUID: {}, 教学楼UUID: {}, 教室名称: {}",
                classroomDO.getClassroomUuid(), getData.getBuildingUuid(), getData.getClassroomName());
    }

    @Override
    public ClassroomInfoDTO getClassroomInfo(String classroomUuid) {
        log.info("获取教室信息 - UUID: {}", classroomUuid);
        // 查询教室
        ClassroomDO classroom = classroomDAO.getById(classroomUuid);
        if (classroom == null) {
            throw new BusinessException("教室不存在: " + classroomUuid, ErrorCode.OPERATION_FAILED);
        }
        // 查询教学楼信息
        BuildingDO building = buildingDAO.getById(classroom.getBuildingUuid());
        if (building == null) {
            throw new BusinessException("教学楼不存在: " + classroom.getBuildingUuid(), ErrorCode.OPERATION_FAILED);
        }
        // 查询教室类型信息
        ClassroomTypeDO classroomType = classroomTypeDAO.getById(classroom.getClassroomTypeUuid());
        if (classroomType == null) {
            throw new BusinessException("教室类型不存在: " + classroom.getClassroomTypeUuid(), ErrorCode.OPERATION_FAILED);
        }
        // 组装返回信息
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
}
