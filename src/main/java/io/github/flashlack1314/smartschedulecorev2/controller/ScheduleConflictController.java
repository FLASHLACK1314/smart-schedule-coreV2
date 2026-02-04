package io.github.flashlack1314.smartschedulecorev2.controller;

import io.github.flashlack1314.smartschedulecorev2.service.ScheduleConflictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 排课冲突控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/scheduleConflict")
public class ScheduleConflictController {
    private final ScheduleConflictService scheduleConflictService;

}
