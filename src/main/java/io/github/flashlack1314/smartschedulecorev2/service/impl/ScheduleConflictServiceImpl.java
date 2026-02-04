package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.ScheduleConflictDAO;
import io.github.flashlack1314.smartschedulecorev2.service.ScheduleConflictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 排课冲突服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleConflictServiceImpl implements ScheduleConflictService {
    private final ScheduleConflictDAO scheduleConflictDAO;

}
