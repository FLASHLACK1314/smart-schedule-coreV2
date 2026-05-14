package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.AutoScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * 执行自动排课（SSE版本，保持连接直到完成）
     *
     * @param params 排课参数
     * @param emitter SSE emitter
     * @return 排课结果
     */
    AutoScheduleResult executeWithSse(AutoScheduleVO params, SseEmitter emitter);

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
     * @return 确认结果，包含冲突数量
     */
    AutoScheduleService.ConfirmResult confirmSchedule(String semesterUuid);

    /**
     * 确认结果
     */
    class ConfirmResult {
        private int conflictCount;
        private String message;

        public ConfirmResult(int conflictCount, String message) {
            this.conflictCount = conflictCount;
            this.message = message;
        }

        public int getConflictCount() {
            return conflictCount;
        }

        public void setConflictCount(int conflictCount) {
            this.conflictCount = conflictCount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 检测并保存排课冲突
     *
     * @param semesterUuid 学期UUID
     * @return 检测到的冲突数量
     */
    int detectAndSaveConflicts(String semesterUuid);

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
