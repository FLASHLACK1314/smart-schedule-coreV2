package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.DepartmentInfoDTO;

/**
 * 学院服务接口
 *
 * @author flash
 */
public interface DepartmentService {

    /**
     * 添加学院
     *
     * @param departmentName 学院名称
     */
    void addDepartment(String departmentName);

    /**
     * 获取学院信息分页
     *
     * @param page           页码
     * @param size           每页数量
     * @param departmentName 学院名称（可选，用于模糊查询）
     * @return 学院信息分页
     */
    PageDTO<DepartmentInfoDTO> getDepartmentPage(int page, int size, String departmentName);

    /**
     * 更新学院信息
     *
     * @param departmentUuid 学院UUID
     * @param departmentName 学院名称
     */
    void updateDepartment(String departmentUuid, String departmentName);

    /**
     * 获取学院信息
     *
     * @param departmentUuid 学院UUID
     * @return 学院信息
     */
    DepartmentInfoDTO getDepartment(String departmentUuid);

    /**
     * 删除学院
     *
     * @param departmentUuid 学院UUID
     */
    void deleteDepartment(String departmentUuid);
}
