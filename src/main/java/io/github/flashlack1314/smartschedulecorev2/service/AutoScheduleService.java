package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;

/**
 * 自动排课服务接口
 *
 * @author flash
 */
public interface AutoScheduleService {

    /**
     * 执行自动排课
     *
     * @param request 排课请求参数
     * @return 排课结果
     */
    AutoScheduleResult autoSchedule(AutoScheduleVO request);

    /**
     * 保存排课方案到数据库（预览状态）
     *
     * @param result  排课结果
     * @param semesterUuid 学期UUID
     */
    void saveScheduleResultAsPreview(AutoScheduleResult result, String semesterUuid);

    /**
     * 确认排课方案（将预览状态转为正式状态）
     *
     * @param semesterUuid 学期UUID
     */
    void confirmSchedule(String semesterUuid);

    /**
     * 清除学期的预览排课方案
     *
     * @param semesterUuid 学期UUID
     */
    void clearPreviewSchedules(String semesterUuid);

    /**
     * 获取排课结果统计信息
     *
     * @param semesterUuid 学期UUID
     * @return 统计信息
     */
    AutoScheduleResult.ScheduleStatistics getScheduleStatistics(String semesterUuid);
}
