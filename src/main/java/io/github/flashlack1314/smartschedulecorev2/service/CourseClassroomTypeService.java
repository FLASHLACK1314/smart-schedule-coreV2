package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseClassroomTypeInfoDTO;

/**
 * 课程类型-教室类型关联服务接口
 *
 * @author flash
 */
public interface CourseClassroomTypeService {

    /**
     * 添加课程类型-教室类型关联
     *
     * @param courseTypeUuid    课程类型UUID
     * @param classroomTypeUuid 教室类型UUID
     */
    void addRelation(String courseTypeUuid, String classroomTypeUuid);

    /**
     * 分页查询关联信息
     *
     * @param page              页码
     * @param size              每页数量
     * @param courseTypeUuid    课程类型UUID（可选过滤）
     * @param classroomTypeUuid 教室类型UUID（可选过滤）
     * @return 分页结果
     */
    PageDTO<CourseClassroomTypeInfoDTO> getRelationPage(int page, int size,
                                                        String courseTypeUuid, String classroomTypeUuid);

    /**
     * 删除关联
     *
     * @param relationUuid 关联UUID
     */
    void deleteRelation(String relationUuid);
}
