package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherInfoDTO;

/**
 * 教师服务
 *
 * @author flash
 */
public interface TeacherService {
    /**
     * 添加教师
     *
     * @param teacherNum       教师工号
     * @param teacherName      教师姓名
     * @param title            职称
     * @param departmentUuid   所属学院UUID
     * @param teacherPassword  密码
     * @param maxHoursPerWeek  每周最高授课时长
     * @param likeTime         教师时间偏好
     * @param isActive         是否启用
     */
    void addTeacher(
            String teacherNum,
            String teacherName,
            String title,
            String departmentUuid,
            String teacherPassword,
            Integer maxHoursPerWeek,
            String likeTime,
            Boolean isActive);

    /**
     * 获取教师信息页
     *
     * @param page           页码
     * @param size           每页数量
     * @param teacherName    教师姓名（可选，模糊查询）
     * @param teacherNum     教师工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 教师信息页
     */
    PageDTO<TeacherInfoDTO> getTeacherPage(
            int page,
            int size,
            String teacherName,
            String teacherNum,
            String departmentUuid);

    /**
     * 更新教师信息
     *
     * @param teacherUuid      教师UUID
     * @param teacherNum       教师工号
     * @param teacherName      教师姓名
     * @param title            职称
     * @param departmentUuid   所属学院UUID
     * @param teacherPassword  密码（可选，为空则不更新）
     * @param maxHoursPerWeek  每周最高授课时长
     * @param likeTime         教师时间偏好
     * @param isActive         是否启用
     */
    void updateTeacher(
            String teacherUuid,
            String teacherNum,
            String teacherName,
            String title,
            String departmentUuid,
            String teacherPassword,
            Integer maxHoursPerWeek,
            String likeTime,
            Boolean isActive);

    /**
     * 获取教师信息
     *
     * @param teacherUuid 教师UUID
     * @return 教师信息
     */
    TeacherInfoDTO getTeacher(String teacherUuid);

    /**
     * 删除教师
     *
     * @param teacherUuid 教师UUID
     */
    void deleteTeacher(String teacherUuid);
}
