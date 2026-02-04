package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseTypeInfoDTO;

/**
 * 课程类型服务接口
 *
 * @author flash
 */
public interface CourseTypeService {

    /**
     * 添加课程类型
     *
     * @param courseTypeName 课程类型名称
     */
    void addCourseType(String courseTypeName);

    /**
     * 获取课程类型信息分页
     *
     * @param page           页码
     * @param size           每页数量
     * @param courseTypeName 课程类型名称（可选，用于模糊查询）
     * @return 课程类型信息分页
     */
    PageDTO<CourseTypeInfoDTO> getCourseTypePage(int page, int size, String courseTypeName);

    /**
     * 更新课程类型信息
     *
     * @param courseTypeUuid 课程类型UUID
     * @param courseTypeName 课程类型名称
     */
    void updateCourseType(String courseTypeUuid, String courseTypeName);

    /**
     * 获取课程类型信息
     *
     * @param courseTypeUuid 课程类型UUID
     * @return 课程类型信息
     */
    CourseTypeInfoDTO getCourseType(String courseTypeUuid);

    /**
     * 删除课程类型
     *
     * @param courseTypeUuid 课程类型UUID
     */
    void deleteCourseType(String courseTypeUuid);
}
