package io.github.flashlack1314.smartschedulecorev2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Token配置属性
 *
 * @author flash
 */
@Data
@Component
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    /**
     * Token有效期（默认12小时）
     */
    private Duration expiration = Duration.ofHours(12);

    /**
     * Token长度（默认64位）
     */
    private Integer tokenLength = 64;

    /**
     * 是否记录设备信息
     */
    private Boolean recordDeviceInfo = true;

    /**
     * 是否允许多设备登录
     */
    private Boolean allowMultipleDevices = true;

    /**
     * Redis Key前缀
     */
    private String redisKeyPrefix = "auth:token:";

    /**
     * 用户Token索引Key前缀
     */
    private String userTokensPrefix = "auth:user_tokens:";
}