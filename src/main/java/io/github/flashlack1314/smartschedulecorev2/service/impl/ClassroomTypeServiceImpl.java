package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.dao.ClassroomTypeDAO;
import io.github.flashlack1314.smartschedulecorev2.service.ClassroomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 教室类型服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomTypeServiceImpl implements ClassroomTypeService {
    private final ClassroomTypeDAO classroomTypeDAO;

}
