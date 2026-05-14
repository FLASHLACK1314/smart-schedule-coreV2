package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScoreInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScoreStatisticsDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.BatchAddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.UpdateScoreVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩服务接口
 *
 * @author flash
 */
public interface ScoreService {

    /**
     * 添加成绩
     *
     * @param getData 添加成绩信息
     * @return 成绩UUID
     */
    String addScore(AddScoreVO getData);

    /**
     * 批量添加成绩
     *
     * @param getData 批量添加成绩信息
     * @return 成功添加的数量
     */
    int batchAddScore(BatchAddScoreVO getData);

    /**
     * 更新成绩
     *
     * @param getData 更新成绩信息
     */
    void updateScore(UpdateScoreVO getData);

    /**
     * 删除成绩
     *
     * @param scoreUuid 成绩UUID
     */
    void deleteScore(String scoreUuid);

    /**
     * 获取成绩信息
     *
     * @param scoreUuid 成绩UUID
     * @return 成绩信息
     */
    ScoreInfoDTO getScore(String scoreUuid);

    /**
     * 分页查询成绩
     *
     * @param page             页码
     * @param size             每页数量
     * @param studentUuid      学生UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param semesterUuid     学期UUID（可选）
     * @return 分页结果
     */
    PageDTO<ScoreInfoDTO> getScorePage(int page, int size, String studentUuid,
                                        String teachingClassUuid, String semesterUuid);

    /**
     * 查询学生成绩列表
     *
     * @param studentUuid 学生UUID
     * @param semesterUuid 学期UUID（可选）
     * @return 成绩列表
     */
    List<ScoreInfoDTO> getStudentScores(String studentUuid, String semesterUuid);

    /**
     * 查询教学班成绩列表
     *
     * @param teachingClassUuid 教学班UUID
     * @return 成绩列表
     */
    List<ScoreInfoDTO> getTeachingClassScores(String teachingClassUuid);

    /**
     * 获取成绩统计信息
     *
     * @param teachingClassUuid 教学班UUID
     * @return 统计信息
     */
    ScoreStatisticsDTO getScoreStatistics(String teachingClassUuid);

    /**
     * 计算学生绩点
     *
     * @param studentUuid  学生UUID
     * @param semesterUuid 学期UUID（可选，为空则计算所有学期）
     * @return 平均绩点
     */
    BigDecimal calculateGPA(String studentUuid, String semesterUuid);

    /**
     * 自动初始化教学班的学生成绩（成绩预设为0）
     * 当教学班创建或确认排课后，自动为所有关联行政班的学生创建成绩记录
     *
     * @param teachingClassUuid 教学班UUID
     * @param semesterUuid      学期UUID
     * @return 初始化成绩的学生数量
     */
    int initScoresForTeachingClass(String teachingClassUuid, String semesterUuid);
}
