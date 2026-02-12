package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddStudentVO;

/**
 * 学生服务接口
 *
 * @author flash
 */
public interface StudentService {
    /**
     * 添加学生
     *
     * @param getData 添加学生信息
     */
    void addStudent(AddStudentVO getData);

    /**
     * 分页查询学生
     *
     * @param page          页码
     * @param size          每页数量
     * @param studentName   学生姓名
     * @param studentId     学号
     * @param classUuid     班级UUID
     * @param majorUuid     专业UUID
     * @param departmentUuid 学院UUID
     * @return 分页结果
     */
    PageDTO<StudentInfoDTO> getStudentPage(int page, int size, String studentName, String studentId,
                                           String classUuid, String majorUuid, String departmentUuid);

    /**
     * 获取学生信息
     *
     * @param studentUuid 学生UUID
     * @return 学生信息
     */
    StudentInfoDTO getStudent(String studentUuid);

    /**
     * 更新学生信息
     *
     * @param studentUuid     学生UUID
     * @param studentId       学号
     * @param studentName     学生姓名
     * @param classUuid       班级UUID
     * @param studentPassword 学生密码（可选）
     */
    void updateStudent(String studentUuid, String studentId, String studentName, String classUuid, String studentPassword);

    /**
     * 删除学生
     *
     * @param studentUuid 学生UUID
     */
    void deleteStudent(String studentUuid);
}
