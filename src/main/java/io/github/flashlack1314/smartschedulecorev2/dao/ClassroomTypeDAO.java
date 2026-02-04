package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ClassroomTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassroomTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教室类型DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class ClassroomTypeDAO extends ServiceImpl<ClassroomTypeMapper, ClassroomTypeDO>
        implements IService<ClassroomTypeDO> {

    /**
     * 根据教室类型名称获取教室类型
     *
     * @param classroomTypeName 教室类型名称
     * @return 教室类型实体
     */
    public ClassroomTypeDO getByClassroomTypeName(String classroomTypeName) {
        LambdaQueryWrapper<ClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomTypeDO::getTypeName, classroomTypeName);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查教室类型名称是否存在
     *
     * @param classroomTypeName 教室类型名称
     * @return 是否存在
     */
    public boolean existsByClassroomTypeName(String classroomTypeName) {
        LambdaQueryWrapper<ClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomTypeDO::getTypeName, classroomTypeName);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 检查教室类型名称是否被其他教室类型使用
     *
     * @param classroomTypeName 教室类型名称
     * @param excludeUuid       排除的UUID
     * @return 是否存在
     */
    public boolean existsByClassroomTypeNameExcludeUuid(String classroomTypeName, String excludeUuid) {
        LambdaQueryWrapper<ClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomTypeDO::getTypeName, classroomTypeName);
        queryWrapper.ne(ClassroomTypeDO::getClassroomTypeUuid, excludeUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询教室类型
     *
     * @param page              页码
     * @param size              每页数量
     * @param classroomTypeName 教室类型名称（可选，模糊查询）
     * @return 分页结果
     */
    public IPage<ClassroomTypeDO> getClassroomTypePage(int page, int size, String classroomTypeName) {
        LambdaQueryWrapper<ClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        if (classroomTypeName != null && !classroomTypeName.trim().isEmpty()) {
            queryWrapper.like(ClassroomTypeDO::getTypeName, classroomTypeName);
        }
        return this.page(new Page<>(page, size), queryWrapper);
    }

    /**
     * 检查教室类型UUID是否存在
     *
     * @param classroomTypeUuid 教室类型UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByUuid(String classroomTypeUuid) {
        return this.getById(classroomTypeUuid) != null;
    }
}