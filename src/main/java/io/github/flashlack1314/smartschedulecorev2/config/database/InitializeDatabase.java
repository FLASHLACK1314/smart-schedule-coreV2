package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.SystemAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SystemAdminDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据库数据初始化类
 * 用于在表结构创建完成后初始化基础数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeDatabase {

    private final SystemAdminDAO adminDAO;

    /**
     * 初始化数据库数据
     * 在表结构创建完成后自动调用
     */
    public void initializeDatabase() {
        log.info("开始初始化数据库基础数据...");
        this.createSystemAdmin();
        log.info("数据库基础数据初始化完成");
    }

    /**
     * 创建默认系统管理员
     */
    private void createSystemAdmin() {
        log.info("正在创建系统管理员...");
        SystemAdminDO systemAdminDO = new SystemAdminDO();
        systemAdminDO.setAdminUuid(UuidUtil.generateUuidNoDash())
                .setAdminUsername("admin")
                .setAdminPassword(PasswordUtil.encrypt("qwer1234"));
        adminDAO.save(systemAdminDO);
        log.info("系统管理员创建成功 - 用户名: admin, 密码: qwer1234");
    }
}