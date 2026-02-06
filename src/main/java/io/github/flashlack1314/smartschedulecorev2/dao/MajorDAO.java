package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.MajorMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.MajorDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 专业DAO
 * @author flash
 */
@Slf4j
@Repository
public class MajorDAO extends ServiceImpl<MajorMapper, MajorDO>
        implements IService<MajorDO> {

    /**
     * 检查学院下是否存在专业
     *
     * @param departmentUuid 学院UUID
     * @return 是否存在
     */
    public boolean existsByDepartmentUuid(String departmentUuid) {
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MajorDO::getDepartmentUuid, departmentUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计学院下的专业数量
     *
     * @param departmentUuid 学院UUID
     * @return 专业数量
     */
    public long countByDepartmentUuid(String departmentUuid) {
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MajorDO::getDepartmentUuid, departmentUuid);
        return this.count(queryWrapper);
    }

    /**
     * 根据专业编号查询专业
     *
     * @param majorNum 专业编号
     * @return 专业实体，如果不存在则返回null
     */
    public MajorDO getByMajorNum(String majorNum) {
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MajorDO::getMajorNum, majorNum);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查专业编号是否已存在
     *
     * @param majorNum 专业编号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByMajorNum(String majorNum) {
        return getByMajorNum(majorNum) != null;
    }

    /**
     * 检查专业编号是否被其他专业使用（排除指定UUID的专业）
     *
     * @param majorNum 专业编号
     * @param excludeUuid 要排除的专业UUID
     * @return 如果被其他专业使用返回true，否则返回false
     */
    public boolean existsByMajorNumExcludeUuid(String majorNum, String excludeUuid) {
        MajorDO major = getByMajorNum(majorNum);
        return major != null && !major.getMajorUuid().equals(excludeUuid);
    }

    /**
     * 检查专业UUID是否存在
     *
     * @param majorUuid 专业UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByUuid(String majorUuid) {
        return this.getById(majorUuid) != null;
    }

    /**
     * 分页查询专业信息
     *
     * @param page           页码
     * @param size           每页数量
     * @param departmentUuid 学院UUID（可选，用于精确查询）
     * @param majorNum       专业编号（可选，用于模糊查询）
     * @param majorName      专业名称（可选，用于模糊查询）
     * @return 分页结果
     */
    public IPage<MajorDO> getMajorPage(int page, int size, String departmentUuid, String majorNum, String majorName) {
        log.debug("DAO层查询专业分页 - page: {}, size: {}, departmentUuid: {}, majorNum: {}, majorName: {}",
                page, size, departmentUuid, majorNum, majorName);

        // 构建查询条件
        LambdaQueryWrapper<MajorDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加精确查询条件
        if (departmentUuid != null && !departmentUuid.trim().isEmpty()) {
            queryWrapper.eq(MajorDO::getDepartmentUuid, departmentUuid);
        }

        // 添加模糊查询条件
        if (majorNum != null && !majorNum.trim().isEmpty()) {
            queryWrapper.like(MajorDO::getMajorNum, majorNum);
        }
        if (majorName != null && !majorName.trim().isEmpty()) {
            queryWrapper.like(MajorDO::getMajorName, majorName);
        }

        // 执行分页查询
        return this.page(new Page<>(page, size), queryWrapper);
    }
}
