package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.TeacherMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 教师DAO
 * @author flash
 */
@Slf4j
@Repository
public class TeacherDAO extends ServiceImpl<TeacherMapper, TeacherDO>
        implements IService<TeacherDO> {

    /**
     * 根据教师工号查询教师
     *
     * @param teacherNum 教师工号
     * @return 教师实体，如果不存在则返回null
     */
    public TeacherDO getByTeacherNum(String teacherNum) {
        LambdaQueryWrapper<TeacherDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeacherDO::getTeacherNum, teacherNum);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查教师工号是否已存在
     *
     * @param teacherNum 教师工号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByTeacherNum(String teacherNum) {
        return getByTeacherNum(teacherNum) != null;
    }

    /**
     * 检查教师工号是否被其他教师使用（排除指定UUID）
     *
     * @param teacherNum  教师工号
     * @param teacherUuid 要排除的教师UUID
     * @return 是否存在
     */
    public boolean existsByTeacherNumExcludeUuid(String teacherNum, String teacherUuid) {
        LambdaQueryWrapper<TeacherDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeacherDO::getTeacherNum, teacherNum)
                .ne(TeacherDO::getTeacherUuid, teacherUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询教师
     *
     * @param page           页码
     * @param size           每页数量
     * @param teacherName    教师姓名（可选，模糊查询）
     * @param teacherNum     教师工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 分页结果
     */
    public IPage<TeacherDO> getTeacherPage(int page, int size, String teacherName, String teacherNum, String departmentUuid) {
        Page<TeacherDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TeacherDO> queryWrapper = new LambdaQueryWrapper<>();

        // 教师姓名模糊查询
        if (StringUtils.hasText(teacherName)) {
            queryWrapper.like(TeacherDO::getTeacherName, teacherName);
        }

        // 教师工号模糊查询
        if (StringUtils.hasText(teacherNum)) {
            queryWrapper.like(TeacherDO::getTeacherNum, teacherNum);
        }

        // 学院UUID精确查询
        if (StringUtils.hasText(departmentUuid)) {
            queryWrapper.eq(TeacherDO::getDepartmentUuid, departmentUuid);
        }

        return this.page(pageParam, queryWrapper);
    }
}
