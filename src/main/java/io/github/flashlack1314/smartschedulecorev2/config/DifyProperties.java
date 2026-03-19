package io.github.flashlack1314.smartschedulecorev2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify 配置属性
 *
 * @author flash
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {

    /**
     * Dify API 基础地址（如 <a href="http://172.16.10.2/v1">...</a>）
     */
    private String baseUrl;

    /**
     * Dify 应用的 API Key
     */
    private String apiKey;

    /**
     * 连接超时时间（毫秒，默认 5000）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒，默认 60000，AI 响应可能较慢）
     */
    private Integer readTimeout = 60000;

    /**
     * 写入超时时间（毫秒，默认 30000）
     */
    private Integer writeTimeout = 30000;
}
