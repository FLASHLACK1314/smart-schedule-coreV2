package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.DepartmentMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学院DAO
 * @author flash
 */
@Slf4j
@Repository
public class DepartmentDAO extends ServiceImpl<DepartmentMapper, DepartmentDO>
        implements IService<DepartmentDO> {

    /**
     * 根据学院名称获取学院
     *
     * @param departmentName 学院名称
     * @return 学院实体
     */
    public DepartmentDO getByDepartmentName(String departmentName) {
        LambdaQueryWrapper<DepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepartmentDO::getDepartmentName, departmentName);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查学院名称是否存在
     *
     * @param departmentName 学院名称
     * @return 是否存在
     */
    public boolean existsByDepartmentName(String departmentName) {
        LambdaQueryWrapper<DepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepartmentDO::getDepartmentName, departmentName);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 检查学院名称是否被其他学院使用
     *
     * @param departmentName 学院名称
     * @param excludeUuid    排除的UUID
     * @return 是否存在
     */
    public boolean existsByDepartmentNameExcludeUuid(String departmentName, String excludeUuid) {
        LambdaQueryWrapper<DepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepartmentDO::getDepartmentName, departmentName);
        queryWrapper.ne(DepartmentDO::getDepartmentUuid, excludeUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询学院
     *
     * @param page           页码
     * @param size           每页数量
     * @param departmentName 学院名称（可选，模糊查询）
     * @return 分页结果
     */
    public IPage<DepartmentDO> getDepartmentPage(int page, int size, String departmentName) {
        LambdaQueryWrapper<DepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        if (departmentName != null && !departmentName.trim().isEmpty()) {
            queryWrapper.like(DepartmentDO::getDepartmentName, departmentName);
        }
        return this.page(new Page<>(page, size), queryWrapper);
    }
}
