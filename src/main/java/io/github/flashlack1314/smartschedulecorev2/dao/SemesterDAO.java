package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.SemesterMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SemesterDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学期DAO
 * @author flash
 */
@Slf4j
@Repository
public class SemesterDAO extends ServiceImpl<SemesterMapper, SemesterDO>
        implements IService<SemesterDO> {

    /**
     * 根据学期名称查询学期
     *
     * @param semesterName 学期名称
     * @return 学期实体，如果不存在则返回null
     */
    public SemesterDO getBySemesterName(String semesterName) {
        LambdaQueryWrapper<SemesterDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SemesterDO::getSemesterName, semesterName);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查学期名称是否已存在
     *
     * @param semesterName 学期名称
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsBySemesterName(String semesterName) {
        return getBySemesterName(semesterName) != null;
    }

    /**
     * 检查学期名称是否被其他学期使用（排除指定UUID的学期）
     *
     * @param semesterName 学期名称
     * @param excludeUuid  要排除的学期UUID
     * @return 如果被其他学期使用返回true，否则返回false
     */
    public boolean existsBySemesterNameExcludeUuid(String semesterName, String excludeUuid) {
        SemesterDO semester = getBySemesterName(semesterName);
        return semester != null && !semester.getSemesterUuid().equals(excludeUuid);
    }

    /**
     * 检查学期UUID是否存在
     *
     * @param semesterUuid 学期UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByUuid(String semesterUuid) {
        return this.getById(semesterUuid) != null;
    }

    /**
     * 分页查询学期信息
     *
     * @param page         页码
     * @param size         每页数量
     * @param semesterName 学期名称（可选，用于模糊查询）
     * @return 分页结果
     */
    public IPage<SemesterDO> getSemesterPage(int page, int size, String semesterName) {
        log.debug("DAO层查询学期分页 - page: {}, size: {}, semesterName: {}",
                page, size, semesterName);

        // 构建查询条件
        LambdaQueryWrapper<SemesterDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加模糊查询条件
        if (semesterName != null && !semesterName.trim().isEmpty()) {
            queryWrapper.like(SemesterDO::getSemesterName, semesterName);
        }

        // 执行分页查询
        return this.page(new Page<>(page, size), queryWrapper);
    }
}
