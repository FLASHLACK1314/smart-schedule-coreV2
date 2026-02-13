package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminInfoDTO;

/**
 * 教务服务
 *
 * @author flash
 */
public interface AcademicService {

    /**
     * 添加教务管理员
     *
     * @param academicNum      教务工号
     * @param academicName     教务名称
     * @param departmentUuid   所属学院UUID
     * @param academicPassword 密码
     */
    void addAcademicAdmin(String academicNum, String academicName, String departmentUuid, String academicPassword);

    /**
     * 获取教务管理员分页信息
     *
     * @param page           页码
     * @param size           每页数量
     * @param academicName   教务名称（可选，模糊查询）
     * @param academicNum    教务工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 分页结果
     */
    PageDTO<AcademicAdminInfoDTO> getAcademicAdminPage(int page, int size, String academicName,
                                                        String academicNum, String departmentUuid);

    /**
     * 更新教务管理员信息
     *
     * @param academicUuid     教务UUID
     * @param academicNum      教务工号
     * @param academicName     教务名称
     * @param departmentUuid   所属学院UUID
     * @param academicPassword 密码（可选，为空则不更新）
     */
    void updateAcademicAdmin(String academicUuid, String academicNum, String academicName,
                              String departmentUuid, String academicPassword);

    /**
     * 获取单个教务管理员信息
     *
     * @param academicUuid 教务UUID
     * @return 教务信息
     */
    AcademicAdminInfoDTO getAcademicAdmin(String academicUuid);

    /**
     * 删除教务管理员
     *
     * @param academicUuid 教务UUID
     */
    void deleteAcademicAdmin(String academicUuid);
}
