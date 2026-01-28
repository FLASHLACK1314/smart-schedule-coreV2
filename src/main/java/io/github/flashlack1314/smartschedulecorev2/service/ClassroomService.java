package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.ClassroomInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddClassroomVO;

/**
 * 教室服务接口
 *
 * @author flash
 */
public interface ClassroomService {
    /**
     * 新增教室
     *
     * @param getData 新增教室信息
     */
    void addClassroom(
            AddClassroomVO getData);

    /**
     * 获取教室信息
     *
     * @param classroomUuid 教室uuid
     * @return 教室信息
     */
    ClassroomInfoDTO getClassroomInfo(
            String classroomUuid);
}
