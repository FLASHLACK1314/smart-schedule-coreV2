package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseQualificationInfoDTO;

/**
 * 课程教师资格关联服务接口
 *
 * @author flash
 */
public interface CourseQualificationService {

    /**
     * 添加课程-教师资格关联
     *
     * @param courseUuid  课程UUID
     * @param teacherUuid 教师UUID
     */
    void addQualification(String courseUuid, String teacherUuid);

    /**
     * 获取课程教师资格关联分页信息
     *
     * @param page        页码
     * @param size        每页数量
     * @param courseUuid  课程UUID（可选过滤）
     * @param teacherUuid 教师UUID（可选过滤）
     * @return 资格关联分页信息
     */
    PageDTO<CourseQualificationInfoDTO> getQualificationPage(int page, int size,
                                                             String courseUuid, String teacherUuid);

    /**
     * 删除课程-教师资格关联
     *
     * @param qualificationUuid 关联UUID
     */
    void deleteQualification(String qualificationUuid);
}
