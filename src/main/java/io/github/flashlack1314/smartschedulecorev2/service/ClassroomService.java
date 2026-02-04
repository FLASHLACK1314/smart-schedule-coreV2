package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.ClassroomInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
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

    /**
     * 教室信息分页查询
     *
     * @param page              页码
     * @param size              每页数量
     * @param buildingUuid      教学楼uuid
     * @param classroomName     教室名称
     * @param classroomCapacity 教室容量
     * @param classroomTypeUuid 教室类型uuid
     * @return
     */

    PageDTO<ClassroomInfoDTO> getClassroomPageList(
            int page,
            int size,
            String buildingUuid,
            String classroomName,
            String classroomCapacity,
            String classroomTypeUuid);

    /**
     * 更新教室信息
     *
     * @param classroomUuid     教室UUID
     * @param buildingUuid      教学楼UUID
     * @param classroomName     教室名称
     * @param classroomCapacity 教室容量
     * @param classroomTypeUuid 教室类型UUID
     */
    void updateClassroom(
            String classroomUuid,
            String buildingUuid,
            String classroomName,
            Integer classroomCapacity,
            String classroomTypeUuid);

    /**
     * 删除教室
     *
     * @param classroomUuid 教室UUID
     */
    void deleteClassroom(String classroomUuid);
}
