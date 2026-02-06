package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.StudentMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学生DAO
 * @author flash
 */
@Slf4j
@Repository
public class StudentDAO extends ServiceImpl<StudentMapper, StudentDO>
        implements IService<StudentDO> {

    /**
     * 根据学号查询学生
     *
     * @param studentId 学号
     * @return 学生实体，如果不存在则返回null
     */
    public StudentDO getByStudentId(String studentId) {
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentDO::getStudentId, studentId);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查学号是否已存在
     *
     * @param studentId 学号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByStudentId(String studentId) {
        return getByStudentId(studentId) != null;
    }

    /**
     * 检查班级下是否存在学生
     *
     * @param classUuid 班级UUID
     * @return 是否存在
     */
    public boolean existsByClassUuid(String classUuid) {
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentDO::getClassUuid, classUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计班级下的学生数量
     *
     * @param classUuid 班级UUID
     * @return 学生数量
     */
    public long countByClassUuid(String classUuid) {
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentDO::getClassUuid, classUuid);
        return this.count(queryWrapper);
    }
}
