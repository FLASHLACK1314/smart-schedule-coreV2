package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.StudentMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

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

    /**
     * 分页查询学生（支持多级筛选）
     *
     * @param page          页码
     * @param size          每页数量
     * @param studentName   学生姓名（模糊查询）
     * @param studentId     学号（模糊查询）
     * @param classUuid     班级UUID（精确查询）
     * @param classUuidList 符合专业/学院筛选条件的班级UUID列表（可为null表示不筛选）
     * @return 分页结果
     */
    public IPage<StudentDO> getStudentPage(int page, int size, String studentName, String studentId,
                                           String classUuid, List<String> classUuidList) {
        Page<StudentDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(studentName)) {
            queryWrapper.like(StudentDO::getStudentName, studentName);
        }
        if (StringUtils.hasText(studentId)) {
            queryWrapper.like(StudentDO::getStudentId, studentId);
        }
        if (StringUtils.hasText(classUuid)) {
            queryWrapper.eq(StudentDO::getClassUuid, classUuid);
        } else if (classUuidList != null && !classUuidList.isEmpty()) {
            // 如果指定了专业或学院，按班级UUID列表筛选
            queryWrapper.in(StudentDO::getClassUuid, classUuidList);
        }

        return this.page(pageParam, queryWrapper);
    }

    /**
     * 检查学号是否被其他学生使用（排除指定UUID）
     *
     * @param studentId   学号
     * @param excludeUuid 要排除的学生UUID
     * @return 如果被其他学生使用返回true，否则返回false
     */
    public boolean existsByStudentIdExcludeUuid(String studentId, String excludeUuid) {
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentDO::getStudentId, studentId)
                .ne(StudentDO::getStudentUuid, excludeUuid);
        return this.count(queryWrapper) > 0;
    }
}
