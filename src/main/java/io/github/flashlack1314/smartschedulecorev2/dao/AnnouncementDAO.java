package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.AnnouncementMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AnnouncementDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统公告DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class AnnouncementDAO extends ServiceImpl<AnnouncementMapper, AnnouncementDO>
        implements IService<AnnouncementDO> {

    /**
     * 根据用户类型获取公告列表
     *
     * @param userType 用户类型
     * @param limit    限制条数
     * @return 公告列表
     */
    public List<AnnouncementDO> getAnnouncementsByUserType(String userType, int limit) {
        LambdaQueryWrapper<AnnouncementDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AnnouncementDO::getUserType, userType)
                .orderByDesc(AnnouncementDO::getCreatedAt)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }
}
