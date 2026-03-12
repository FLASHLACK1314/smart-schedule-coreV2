package io.github.flashlack1314.smartschedulecorev2.mcp.config;

import io.github.flashlack1314.smartschedulecorev2.mcp.EduScheduleTool;
import io.github.flashlack1314.smartschedulecorev2.mcp.tools.QueryTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具配置类
 * 注册所有 MCP 工具到 Spring AI
 *
 * @author flash
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpConfig {

    private final QueryTools queryTools;
    private final EduScheduleTool eduScheduleTool;

    /**
     * 注册 MCP 工具提供者
     * Spring AI 会自动扫描并注册这些工具到 MCP Server
     */
    @Bean
    public ToolCallbackProvider toolProvider() {
        log.info("注册 MCP 工具: QueryTools, EduScheduleTool");
        return MethodToolCallbackProvider.builder()
                .toolObjects(queryTools, eduScheduleTool)
                .build();
    }
}
