package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScheduleConflictInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * 排课冲突服务接口
 *
 * @author flash
 */
public interface ScheduleConflictService {

    /**
     * 分页查询排课冲突记录
     *
     * @param page         页码
     * @param size         每页数量
     * @param semesterUuid 学期UUID（可选）
     * @param severity     严重程度：1-硬冲突, 0-软冲突（可选）
     * @param conflictType 冲突类型（可选）
     * @return 分页结果
     */
    PageDTO<ScheduleConflictInfoDTO> getConflictPage(int page, int size, String semesterUuid,
                                                      Integer severity, String conflictType);

    /**
     * 获取冲突详情
     *
     * @param conflictUuid 冲突UUID
     * @return 冲突详情
     */
    ScheduleConflictInfoDTO getConflict(String conflictUuid);

    /**
     * 获取冲突统计信息
     *
     * @param semesterUuid 学期UUID（可选）
     * @return 统计信息
     */
    Map<String, Object> getConflictStats(String semesterUuid);

    /**
     * 删除冲突记录
     *
     * @param conflictUuid 冲突UUID
     */
    void deleteConflict(String conflictUuid);

    /**
     * 根据排课UUID查询相关冲突
     *
     * @param scheduleUuid 排课UUID
     * @return 冲突列表
     */
    List<ScheduleConflictInfoDTO> getConflictsByScheduleUuid(String scheduleUuid);
}
