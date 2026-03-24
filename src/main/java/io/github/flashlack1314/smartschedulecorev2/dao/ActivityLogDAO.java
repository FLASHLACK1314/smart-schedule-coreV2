package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ActivityLogMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ActivityLogDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
