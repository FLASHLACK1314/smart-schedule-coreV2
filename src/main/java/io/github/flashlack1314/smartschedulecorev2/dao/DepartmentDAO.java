package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.DepartmentMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 学院DAO
 * @author flash
 */
@Slf4j
@Repository
public class DepartmentDAO extends ServiceImpl<DepartmentMapper, DepartmentDO>
        implements IService<DepartmentDO> {
}
