package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.enums.ActionType;

/**
 * 活动日志服务
 *
 * @author flash
 */
public interface ActivityLogService {

    /**
     * 记录活动日志
     *
     * @param token      用户Token
     * @param actionType 操作类型
     */
    void logActivity(String token, ActionType actionType);

    /**
     * 记录活动日志（指定用户信息）
     *
     * @param userUuid   用户UUID
     * @param userName   用户名称
     * @param actionType 操作类型
     */
    void logActivity(String userUuid, String userName, ActionType actionType);
}
