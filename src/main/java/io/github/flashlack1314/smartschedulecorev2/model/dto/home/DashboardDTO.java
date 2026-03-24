package io.github.flashlack1314.smartschedulecorev2.model.dto.home;

import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 首页聚合数据DTO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class DashboardDTO {

    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;

    /**
     * 统计数据
     */
    private StatsDTO stats;

    /**
     * 最近活动列表(学生用户为空)
     */
    private List<ActivityDTO> recentActivities;

    /**
     * 今日课程列表
     */
    private List<TodayCourseDTO> todayCourses;

    /**
     * 系统公告列表(按角色过滤)
     */
    private List<AnnouncementDTO> announcements;

    /**
     * 用户信息内部类
     */
    @Data
    @Accessors(chain = true)
    public static class UserInfoDTO {
        /**
         * 用户名称
         */
        private String name;

        /**
         * 用户类型
         */
        private UserType userType;
    }
}
