package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.home.*;

import java.util.List;

/**
 * 首页服务
 *
 * @author flash
 */
public interface HomeService {

    /**
     * 获取首页聚合数据
     *
     * @param token 认证Token
     * @return 首页聚合数据
     */
    DashboardDTO getDashboard(String token);

    /**
     * 获取最近活动列表
     *
     * @param limit 限制条数
     * @return 活动列表
     */
    List<ActivityDTO> getActivities(int limit);

    /**
     * 获取今日课程
     *
     * @param token 认证Token
     * @return 今日课程列表
     */
    List<TodayCourseDTO> getTodayCourses(String token);

    /**
     * 获取系统公告
     *
     * @param token 认证Token
     * @param limit 限制条数
     * @return 公告列表
     */
    List<AnnouncementDTO> getAnnouncements(String token, int limit);

    /**
     * 发布公告
     *
     * @param dto 公告创建请求DTO
     * @return 创建的公告DTO
     */
    AnnouncementDTO createAnnouncement(CreateAnnouncementDTO dto);
}
