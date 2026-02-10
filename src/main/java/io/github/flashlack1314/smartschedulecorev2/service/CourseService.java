package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.CourseInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddCourseVO;

/**
 * 课程服务接口
 *
 * @author flash
 */
public interface CourseService {

    /**
     * 添加课程
     *
     * @param addCourseVO 添加课程信息
     * @return 课程UUID
     */
    String addCourse(AddCourseVO addCourseVO);

    /**
     * 获取课程信息分页
     *
     * @param page           页码
     * @param size           每页数量
     * @param courseName     课程名称（可选，用于模糊查询）
     * @param courseNum      课程编号（可选，用于模糊查询）
     * @param courseTypeUuid 课程类型UUID（可选）
     * @return 课程信息分页
     */
    PageDTO<CourseInfoDTO> getCoursePage(int page, int size, String courseName, String courseNum, String courseTypeUuid);

    /**
     * 更新课程信息
     *
     * @param addCourseVO 更新课程信息
     */
    void updateCourse(AddCourseVO addCourseVO);

    /**
     * 获取课程信息
     *
     * @param courseUuid 课程UUID
     * @return 课程信息
     */
    CourseInfoDTO getCourse(String courseUuid);

    /**
     * 删除课程
     *
     * @param courseUuid 课程UUID
     */
    void deleteCourse(String courseUuid);
}
