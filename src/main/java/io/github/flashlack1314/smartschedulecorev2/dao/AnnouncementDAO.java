package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.AnnouncementMapper;
import io.github.flashlack1314.smartschedulecorev2.model.dto.home.CreateAnnouncementDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AnnouncementDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    /**
     * 发布公告
     *
     * @param dto 公告创建请求DTO
     * @return 创建的公告DO
     */
    public AnnouncementDO createAnnouncement(CreateAnnouncementDTO dto) {
        AnnouncementDO announcement = new AnnouncementDO();
        announcement.setAnnouncementUuid(UUID.randomUUID().toString().replace("-", ""));
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setPriority(dto.getPriority());
        announcement.setUserType(dto.getUserType());
        announcement.setCreatedAt(LocalDateTime.now());
        this.save(announcement);
        return announcement;
    }
}
