package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SemesterInfoDTO;

import java.time.LocalDate;

/**
 * 学期服务接口
 *
 * @author flash
 */
public interface SemesterService {

    /**
     * 添加学期
     *
     * @param semesterName 学期名称
     * @param semesterWeeks 学期周数
     * @param startDate 学期开始日期
     * @param endDate 学期结束日期
     */
    void addSemester(String semesterName, Integer semesterWeeks, LocalDate startDate, LocalDate endDate);

    /**
     * 获取学期信息分页
     *
     * @param page         页码
     * @param size         每页数量
     * @param semesterName 学期名称（可选，用于模糊查询）
     * @return 学期信息分页
     */
    PageDTO<SemesterInfoDTO> getSemesterPage(int page, int size, String semesterName);

    /**
     * 更新学期信息
     *
     * @param semesterUuid 学期UUID
     * @param semesterName 学期名称
     * @param semesterWeeks 学期周数
     * @param startDate 学期开始日期
     * @param endDate 学期结束日期
     */
    void updateSemester(String semesterUuid, String semesterName, Integer semesterWeeks, LocalDate startDate, LocalDate endDate);

    /**
     * 获取学期信息
     *
     * @param semesterUuid 学期UUID
     * @return 学期信息
     */
    SemesterInfoDTO getSemester(String semesterUuid);

    /**
     * 删除学期
     *
     * @param semesterUuid 学期UUID
     */
    void deleteSemester(String semesterUuid);
}
