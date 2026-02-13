package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.AcademicAdminMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 教务管理DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class AcademicAdminDAO extends ServiceImpl<AcademicAdminMapper, AcademicAdminDO>
        implements IService<AcademicAdminDO> {

    /**
     * 根据教务工号查询教务
     *
     * @param academicNum 教务工号
     * @return 教务实体，如果不存在则返回null
     */
    public AcademicAdminDO getByAcademicNum(String academicNum) {
        LambdaQueryWrapper<AcademicAdminDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcademicAdminDO::getAcademicNum, academicNum);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查教务工号是否已存在
     *
     * @param academicNum 教务工号
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByAcademicNum(String academicNum) {
        return getByAcademicNum(academicNum) != null;
    }

    /**
     * 检查教务工号是否被其他教务使用（排除指定UUID）
     *
     * @param academicNum  教务工号
     * @param academicUuid 要排除的教务UUID
     * @return 是否存在
     */
    public boolean existsByAcademicNumExcludeUuid(String academicNum, String academicUuid) {
        LambdaQueryWrapper<AcademicAdminDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcademicAdminDO::getAcademicNum, academicNum)
                .ne(AcademicAdminDO::getAcademicUuid, academicUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询教务
     *
     * @param page           页码
     * @param size           每页数量
     * @param academicName   教务名称（可选，模糊查询）
     * @param academicNum    教务工号（可选，模糊查询）
     * @param departmentUuid 学院UUID（可选）
     * @return 分页结果
     */
    public IPage<AcademicAdminDO> getAcademicAdminPage(int page, int size, String academicName,
                                                        String academicNum, String departmentUuid) {
        Page<AcademicAdminDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AcademicAdminDO> queryWrapper = new LambdaQueryWrapper<>();

        // 教务名称模糊查询
        if (StringUtils.hasText(academicName)) {
            queryWrapper.like(AcademicAdminDO::getAcademicName, academicName);
        }

        // 教务工号模糊查询
        if (StringUtils.hasText(academicNum)) {
            queryWrapper.like(AcademicAdminDO::getAcademicNum, academicNum);
        }

        // 学院UUID精确查询
        if (StringUtils.hasText(departmentUuid)) {
            queryWrapper.eq(AcademicAdminDO::getDepartmentUuid, departmentUuid);
        }

        return this.page(pageParam, queryWrapper);
    }
}
