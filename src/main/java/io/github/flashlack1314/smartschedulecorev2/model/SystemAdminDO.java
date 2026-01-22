package io.github.flashlack1314.smartschedulecorev2.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统管理员DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_system_admin")
public class SystemAdminDO {

    /**
     * 管理员UUID
     */
    @TableId
    private String adminUuid;

    /**
     * 管理员用户名 (唯一登录账号)
     */
    @TableField("admin_username")
    private String adminUsername;

    /**
     * 管理员密码 (建议使用BCrypt等加密存储)
     */
    @TableField("admin_password")
    private String adminPassword;
}
