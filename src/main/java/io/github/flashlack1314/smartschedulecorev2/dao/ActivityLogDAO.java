package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.enums.ActionType;
import io.github.flashlack1314.smartschedulecorev2.mapper.ActivityLogMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ActivityLogDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 活动记录DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class ActivityLogDAO extends ServiceImpl<ActivityLogMapper, ActivityLogDO>
        implements IService<ActivityLogDO> {

    /**
     * 获取最近活动记录
     *
     * @param limit 限制条数
     * @return 活动记录列表
     */
    public List<ActivityLogDO> getRecentActivities(int limit) {
        LambdaQueryWrapper<ActivityLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ActivityLogDO::getCreatedAt)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    /**
     * 获取指定用户的活动记录
     *
     * @param userUuid 用户UUID
     * @param limit    限制条数
     * @return 活动记录列表
     */
    public List<ActivityLogDO> getActivitiesByUserUuid(String userUuid, int limit) {
        LambdaQueryWrapper<ActivityLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityLogDO::getUserUuid, userUuid)
                .orderByDesc(ActivityLogDO::getCreatedAt)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    /**
     * 记录活动日志
     *
     * @param userUuid   用户UUID
     * @param userName   用户名称
     * @param actionType 操作类型
     * @return 是否记录成功
     */
    public boolean logActivity(String userUuid, String userName, ActionType actionType) {
        ActivityLogDO activityLog = new ActivityLogDO();
        activityLog.setActivityUuid(UUID.randomUUID().toString().replace("-", ""));
        activityLog.setUserUuid(userUuid);
        activityLog.setUserName(userName);
        activityLog.setActionType(actionType.name());
        activityLog.setActionText(actionType.getActionText());
        activityLog.setCreatedAt(LocalDateTime.now());
        return this.save(activityLog);
    }
}
