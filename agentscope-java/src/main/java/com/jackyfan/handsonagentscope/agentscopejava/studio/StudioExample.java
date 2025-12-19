package com.jackyfan.handsonagentscope.agentscopejava.studio;

import com.jackyfan.handsonagentscope.agentscopejava.agent.ComprehensiveAgentExample;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.studio.StudioManager;
import io.agentscope.core.studio.StudioMessageHook;
import io.agentscope.core.studio.StudioUserAgent;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

import java.time.LocalDateTime;

public class StudioExample {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        System.out.println("Connecting to Studio at http://localhost:3000...");

        // 初始化 Studio
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("agentscope-java")
                .runName("studio_demo_" + System.currentTimeMillis())
                .initialize()
                .block();
        System.out.println("Connected to Studio\n");

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new TimeTools());

        try {
            // 创建 Agent（带 Studio Hook）
            ReActAgent agent = ReActAgent.builder()
                    .name("Assistant")
                    .sysPrompt("You are a helpful AI assistant.")
                    .model(DashScopeChatModel.builder()
                            .apiKey(apiKey)
                            .modelName("qwen-plus")
                            .build())
                    .toolkit(toolkit)
                    .hook(new StudioMessageHook(StudioManager.getClient()))
                    .build();

            // 创建用户 Agent
            StudioUserAgent user = StudioUserAgent.builder()
                    .name("User")
                    .studioClient(StudioManager.getClient())
                    .webSocketClient(StudioManager.getWebSocketClient())
                    .build();

            // 对话循环
            System.out.println("Starting conversation (type 'exit' to quit)");
            System.out.println("Open http://localhost:3000 to interact\n");

            Msg msg = null;
            int turn = 1;
            while (true) {
                System.out.println("[Turn " + turn + "] Waiting for user input...");
                msg = user.call(msg).block();

                if (msg == null || "exit".equalsIgnoreCase(msg.getTextContent())) {
                    System.out.println("\nConversation ended");
                    break;
                }

                System.out.println("[Turn " + turn + "] User: " + msg.getTextContent());
                msg = agent.call(msg).block();

                if (msg != null) {
                    System.out.println("[Turn " + turn + "] Agent: "
                            + msg.getTextContent() + "\n");
                }
                turn++;
            }

        } finally {
            System.out.println("\nShutting down...");
            StudioManager.shutdown();
            System.out.println("Done\n");
        }
    }

    public static class TimeTools{
        @Tool(description = "获取当前时间")
        public static LocalDateTime getNowTime() {
            return LocalDateTime.now();
        }

        @Tool(description = "获取春节日期")
        public static String getDateOfLunarNewYear() {
            return "2026-02-16";
        }
    }
}
