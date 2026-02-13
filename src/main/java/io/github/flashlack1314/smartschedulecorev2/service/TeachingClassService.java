package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddTeachingClassVO;

/**
 * 教学班服务接口
 *
 * @author flash
 */
public interface TeachingClassService {
    /**
     * 添加教学班
     *
     * @param getData 添加教学班信息
     * @return 教学班UUID
     */
    String addTeachingClass(AddTeachingClassVO getData);

    /**
     * 分页查询教学班
     *
     * @param page         页码
     * @param size         每页数量
     * @param courseUuid   课程UUID（可选）
     * @param teacherUuid  教师UUID（可选）
     * @param semesterUuid 学期UUID（可选）
     * @return 分页结果
     */
    PageDTO<TeachingClassInfoDTO> getTeachingClassPage(int page, int size, String courseUuid,
                                                        String teacherUuid, String semesterUuid);

    /**
     * 获取教学班信息
     *
     * @param teachingClassUuid 教学班UUID
     * @return 教学班信息
     */
    TeachingClassInfoDTO getTeachingClass(String teachingClassUuid);

    /**
     * 更新教学班信息
     *
     * @param getData 更新教学班信息
     */
    void updateTeachingClass(AddTeachingClassVO getData);

    /**
     * 删除教学班
     *
     * @param teachingClassUuid 教学班UUID
     */
    void deleteTeachingClass(String teachingClassUuid);
}
