package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.SystemAdminMapper;
import io.github.flashlack1314.smartschedulecorev2.model.SystemAdminDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 系统管理员DAO
 * @author flash
 */
@Slf4j
@Repository
public class SystemAdminDAO extends ServiceImpl<SystemAdminMapper, SystemAdminDO>
        implements IService<SystemAdminDO> {
}
