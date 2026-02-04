package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.MajorDAO;
import io.github.flashlack1314.smartschedulecorev2.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 专业服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {
    private final MajorDAO majorDAO;

}
