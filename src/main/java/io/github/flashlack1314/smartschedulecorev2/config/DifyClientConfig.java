package io.github.flashlack1314.smartschedulecorev2.config;

import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.model.DifyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dify 客户端配置类
 *
 * @author flash
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DifyClientConfig {

    private final DifyProperties difyProperties;

    /**
     * 创建 DifyChatflowClient Bean
     * <p>
     * 用于与 Dify 工作流编排对话型应用进行交互
     *
     * @return DifyChatflowClient 实例
     */
    @Bean
    public DifyChatflowClient difyChatflowClient() {
        log.info("初始化 DifyChatflowClient - baseUrl: {}", difyProperties.getBaseUrl());

        DifyConfig config = DifyConfig.builder()
                .baseUrl(difyProperties.getBaseUrl())
                .apiKey(difyProperties.getApiKey())
                .connectTimeout(difyProperties.getConnectTimeout())
                .readTimeout(difyProperties.getReadTimeout())
                .writeTimeout(difyProperties.getWriteTimeout())
                .build();

        return DifyClientFactory.createChatWorkflowClient(config);
    }
}
