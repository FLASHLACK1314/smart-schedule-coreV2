package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.CourseTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.service.CourseTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 课程类型服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTypeServiceImpl implements CourseTypeService {
    private final CourseTypeDAO courseTypeDAO;

}
