package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.home.*;
import io.github.flashlack1314.smartschedulecorev2.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/home")
public class HomeController {

    private final HomeService homeService;

    /**
     * 获取首页聚合数据
     *
     * @param token 认证Token
     * @return 首页聚合数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse<DashboardDTO>> getDashboard(
            @RequestHeader("Authorization") String token
    ) {
        DashboardDTO result = homeService.getDashboard(token);
        return ResultUtil.success("获取首页数据成功", result);
    }

    /**
     * 获取最近活动列表
     * <p>
     * 注意：学生不可见此接口
     *
     * @param token 认证Token
     * @param limit 限制条数，默认10
     * @return 活动列表
     */
    @GetMapping("/activities")
    @RequireRole({UserType.TEACHER, UserType.ACADEMIC_ADMIN, UserType.SYSTEM_ADMIN})
    public ResponseEntity<BaseResponse<List<ActivityDTO>>> getActivities(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ActivityDTO> result = homeService.getActivities(limit);
        return ResultUtil.success("获取最近活动成功", result);
    }

    /**
     * 获取今日课程
     *
     * @param token 认证Token
     * @return 今日课程列表
     */
    @GetMapping("/today-courses")
    public ResponseEntity<BaseResponse<TodayCourseResponseDTO>> getTodayCourses(
            @RequestHeader("Authorization") String token
    ) {
        List<TodayCourseDTO> courses = homeService.getTodayCourses(token);
        TodayCourseResponseDTO response = new TodayCourseResponseDTO();
        response.setDate(java.time.LocalDate.now().toString());
        response.setWeekDay(java.time.LocalDate.now().getDayOfWeek().getValue());
        response.setCourses(courses);
        return ResultUtil.success("获取今日课程成功", response);
    }

    /**
     * 获取系统公告
     *
     * @param token 认证Token
     * @param limit 限制条数，默认5
     * @return 公告列表
     */
    @GetMapping("/announcements")
    public ResponseEntity<BaseResponse<AnnouncementResponseDTO>> getAnnouncements(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<AnnouncementDTO> announcements = homeService.getAnnouncements(token, limit);
        AnnouncementResponseDTO response = new AnnouncementResponseDTO();
        response.setAnnouncements(announcements);
        return ResultUtil.success("获取系统公告成功", response);
    }
}
