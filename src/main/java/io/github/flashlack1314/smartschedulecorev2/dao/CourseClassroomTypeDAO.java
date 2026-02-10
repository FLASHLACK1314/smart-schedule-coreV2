package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.CourseClassroomTypeMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.CourseClassroomTypeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 课程类型-教室类型关联DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class CourseClassroomTypeDAO extends ServiceImpl<CourseClassroomTypeMapper, CourseClassroomTypeDO>
        implements IService<CourseClassroomTypeDO> {

    /**
     * 检查课程类型-教室类型关联是否存在
     *
     * @param courseTypeUuid    课程类型UUID
     * @param classroomTypeUuid 教室类型UUID
     * @return 是否存在
     */
    public boolean existsByCourseTypeUuidAndClassroomTypeUuid(String courseTypeUuid, String classroomTypeUuid) {
        LambdaQueryWrapper<CourseClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseClassroomTypeDO::getCourseTypeUuid, courseTypeUuid);
        queryWrapper.eq(CourseClassroomTypeDO::getClassroomTypeUuid, classroomTypeUuid);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 分页查询关联信息
     *
     * @param page              页码
     * @param size              每页数量
     * @param courseTypeUuid    课程类型UUID（可选过滤）
     * @param classroomTypeUuid 教室类型UUID（可选过滤）
     * @return 分页结果
     */
    public IPage<CourseClassroomTypeDO> getRelationPage(int page, int size,
                                                         String courseTypeUuid, String classroomTypeUuid) {
        LambdaQueryWrapper<CourseClassroomTypeDO> queryWrapper = new LambdaQueryWrapper<>();

        if (courseTypeUuid != null && !courseTypeUuid.trim().isEmpty()) {
            queryWrapper.eq(CourseClassroomTypeDO::getCourseTypeUuid, courseTypeUuid);
        }

        if (classroomTypeUuid != null && !classroomTypeUuid.trim().isEmpty()) {
            queryWrapper.eq(CourseClassroomTypeDO::getClassroomTypeUuid, classroomTypeUuid);
        }

        return this.page(new Page<>(page, size), queryWrapper);
    }
}