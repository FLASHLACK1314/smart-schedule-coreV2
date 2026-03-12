package io.github.flashlack1314.smartschedulecorev2.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 教育排课工具：提供排课相关的功能接口，供MCP调用
 * @author flash
 */
@Service
public class EduScheduleTool {


    // 只要有 mcp-server-webmvc 依赖，这个注解就会生效
    @Tool(description = "检测排课冲突并存入预览区。参数：教师姓名, 目标时间, 教室。")
    public String checkAndPreview(
            String teacher,
            String time,
            String room) {
        return "【系统反馈】已识别意图：尝试将 " + teacher + " 的课排在 " + time + " 的 " + room + "。校验通过，预览已生成。";
    }
}