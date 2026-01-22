package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.AcademicAdminMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 教务管理DAO
 * @author flash
 */
@Slf4j
@Repository
public class AcademicAdminDAO extends ServiceImpl<AcademicAdminMapper, AcademicAdminDO>
        implements IService<AcademicAdminDO> {
}
