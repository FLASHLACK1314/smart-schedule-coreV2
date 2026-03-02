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
     * @param params 排课参数
     * @return 排课结果
     */
    AutoScheduleResult execute(AutoScheduleVO params);

    /**
     * 保存排课方案为预览状态
     *
     * @param semesterUuid 学期UUID
     * @param result       排课结果
     */
    void saveAsPreview(String semesterUuid, AutoScheduleResult result);

    /**
     * 确认排课方案（将预览状态转为正式状态）
     *
     * @param semesterUuid 学期UUID
     */
    void confirmSchedule(String semesterUuid);

    /**
     * 清除预览排课方案
     *
     * @param semesterUuid 学期UUID
     */
    void clearPreview(String semesterUuid);

    /**
     * 获取排课统计信息
     *
     * @param semesterUuid 学期UUID
     * @return 统计信息
     */
    AutoScheduleResult.ScheduleStatistics getStatistics(String semesterUuid);
}
