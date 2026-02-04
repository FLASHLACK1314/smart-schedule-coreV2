package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.CourseDAO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 课程服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;

}
