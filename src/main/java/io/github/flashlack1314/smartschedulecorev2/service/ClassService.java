package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassInfoDTO;

/**
 * 行政班级服务
 *
 * @author flash
 */
public interface ClassService {
    /**
     * 添加行政班级
     *
     * @param majorUuid 专业UUID
     * @param className 行政班级名称
     */
    void addClass(String majorUuid, String className);

    /**
     * 获取行政班级信息页
     *
     * @param page           页码
     * @param size           每页数量
     * @param className      行政班级名称（可选，模糊查询）
     * @param majorUuid      专业UUID（可选）
     * @param departmentUuid 学院UUID（可选）
     * @return 行政班级信息页
     */
    PageDTO<ClassInfoDTO> getClassPage(
            int page,
            int size,
            String className,
            String majorUuid,
            String departmentUuid);

    /**
     * 更新行政班级信息
     *
     * @param classUuid 行政班级UUID
     * @param majorUuid 专业UUID
     * @param className 行政班级名称
     */
    void updateClass(
            String classUuid,
            String majorUuid,
            String className);

    /**
     * 获取行政班级信息
     *
     * @param classUuid 行政班级UUID
     * @return 行政班级信息
     */
    ClassInfoDTO getClass(String classUuid);

    /**
     * 删除行政班级
     *
     * @param classUuid 行政班级UUID
     */
    void deleteClass(String classUuid);
}
