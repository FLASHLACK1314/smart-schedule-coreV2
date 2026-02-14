package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScheduleVO;

import java.util.List;

/**
 * 排课服务接口
 *
 * @author flash
 */
public interface ScheduleService {

    /**
     * 添加排课
     *
     * @param getData 排课信息
     * @return 排课UUID
     */
    String addSchedule(AddScheduleVO getData);

    /**
     * 分页查询排课
     *
     * @param page              页码
     * @param size              每页数量
     * @param semesterUuid      学期UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param classroomUuid     教室UUID（可选）
     * @param teacherUuid       教师UUID（可选）
     * @param dayOfWeek         星期几（可选）
     * @param status            状态（可选）
     * @return 分页结果
     */
    PageDTO<ScheduleInfoDTO> getSchedulePage(int page, int size,
            String semesterUuid, String teachingClassUuid, String classroomUuid,
            String teacherUuid, Integer dayOfWeek, Integer status);

    /**
     * 获取排课详情
     *
     * @param scheduleUuid 排课UUID
     * @return 排课详情
     */
    ScheduleInfoDTO getSchedule(String scheduleUuid);

    /**
     * 更新排课
     *
     * @param getData 排课信息
     */
    void updateSchedule(AddScheduleVO getData);

    /**
     * 删除排课
     *
     * @param scheduleUuid 排课UUID
     */
    void deleteSchedule(String scheduleUuid);

    /**
     * 获取教师课表
     *
     * @param teacherUuid  教师UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    List<ScheduleInfoDTO> getTeacherTimetable(String teacherUuid, String semesterUuid);

    /**
     * 获取学生课表
     *
     * @param studentUuid  学生UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    List<ScheduleInfoDTO> getStudentTimetable(String studentUuid, String semesterUuid);

    /**
     * 获取行政班级课表
     *
     * @param classUuid    行政班级UUID
     * @param semesterUuid 学期UUID
     * @return 课表列表
     */
    List<ScheduleInfoDTO> getClassTimetable(String classUuid, String semesterUuid);

    /**
     * 获取教室课表
     *
     * @param classroomUuid 教室UUID
     * @param semesterUuid  学期UUID
     * @return 课表列表
     */
    List<ScheduleInfoDTO> getClassroomTimetable(String classroomUuid, String semesterUuid);
}
