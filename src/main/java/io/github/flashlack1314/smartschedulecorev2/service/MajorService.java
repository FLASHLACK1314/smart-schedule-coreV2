package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.MajorInfoDTO;

/**
 * 专业服务接口
 *
 * @author flash
 */
public interface MajorService {
    /**
     * 添加专业
     *
     * @param departmentUuid 学院UUID
     * @param majorNum       专业编号
     * @param majorName      专业名称
     */
    void addMajor(
            String departmentUuid,
            String majorNum,
            String majorName);

    /**
     * 获取专业信息页
     *
     * @param page           页码
     * @param size           每页数量
     * @param departmentUuid 学院UUID
     * @param majorNum       专业编号
     * @param majorName      专业名称
     * @return 专业信息页
     */
    PageDTO<MajorInfoDTO> getMajorPage(
            int page,
            int size,
            String departmentUuid,
            String majorNum,
            String majorName);

    /**
     * 更新专业信息
     *
     * @param majorUuid      专业UUID
     * @param departmentUuid 学院UUID
     * @param majorNum       专业编号
     * @param majorName      专业名称
     */
    void updateMajor(
            String majorUuid,
            String departmentUuid,
            String majorNum,
            String majorName);

    /**
     * 获取专业信息
     *
     * @param majorUuid 专业UUID
     * @return 专业信息
     */
    MajorInfoDTO getMajor(
            String majorUuid);

    /**
     * 删除专业
     *
     * @param majorUuid 专业UUID
     */
    void deleteMajor(String majorUuid);
}
