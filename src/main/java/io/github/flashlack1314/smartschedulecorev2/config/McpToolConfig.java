package io.github.flashlack1314.smartschedulecorev2.config;

import io.github.flashlack1314.smartschedulecorev2.mcp.EduScheduleTool;
import io.github.flashlack1314.smartschedulecorev2.mcp.tools.QueryTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP工具配置类
 * 将各种工具类注册为MCP工具回调提供者
 *
 * @author flash
 */
@Configuration
public class McpToolConfig {

    /**
     * 注册排课相关的 MCP 工具
     *
     * @param eduScheduleTool 旧示例工具
     * @param queryTools      查询工具
     * @return ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider scheduleTools(EduScheduleTool eduScheduleTool, QueryTools queryTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(eduScheduleTool, queryTools)
                .build();
    }
}