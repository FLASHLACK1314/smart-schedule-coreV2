package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassroomTypeInfoDTO;

/**
 * 教室类型服务接口
 *
 * @author flash
 */
public interface ClassroomTypeService {

    /**
     * 添加教室类型
     *
     * @param classroomTypeName 教室类型名称
     */
    void addClassroomType(String classroomTypeName);

    /**
     * 获取教室类型信息分页
     *
     * @param page              页码
     * @param size              每页数量
     * @param classroomTypeName 教室类型名称（可选，用于模糊查询）
     * @return 教室类型信息分页
     */
    PageDTO<ClassroomTypeInfoDTO> getClassroomTypePage(int page, int size, String classroomTypeName);

    /**
     * 更新教室类型信息
     *
     * @param classroomTypeUuid 教室类型UUID
     * @param classroomTypeName 教室类型名称
     */
    void updateClassroomType(String classroomTypeUuid, String classroomTypeName);

    /**
     * 获取教室类型信息
     *
     * @param classroomTypeUuid 教室类型UUID
     * @return 教室类型信息
     */
    ClassroomTypeInfoDTO getClassroomType(String classroomTypeUuid);

    /**
     * 删除教室类型
     *
     * @param classroomTypeUuid 教室类型UUID
     */
    void deleteClassroomType(String classroomTypeUuid);
}
