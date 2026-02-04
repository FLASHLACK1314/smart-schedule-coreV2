package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.TeachingClassDAO;
import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 教学班服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingClassServiceImpl implements TeachingClassService {
    private final TeachingClassDAO teachingClassDAO;

}
