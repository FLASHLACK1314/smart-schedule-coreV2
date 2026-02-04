package io.github.flashlack1314.smartschedulecorev2.service;

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
}
