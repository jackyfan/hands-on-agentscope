package com.jackyfan.handsonagentscope.agentscopejava.session;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.SessionManager;
import io.agentscope.core.studio.StudioManager;

import java.nio.file.Path;
import java.util.List;

public class SessionExample {
    public static void main(String[] args) {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("agentscope-java")
                .runName("SessionExample")
                .initialize()
                .block();
        // 会话 ID（例如，用户 ID、对话 ID）
        String sessionId = "user-alice-chat-001";
        Path sessionPath = Path.of("./sessions");

        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-plus")
                .build();

        // 创建智能体
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的助手。记住之前的对话。")
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        // 尝试加载现有会话
        SessionManager.forSessionId(sessionId)
                .withSession(new JsonSession(sessionPath))
                .addComponent(agent)
                .loadIfExists();

        // 与智能体交互
        Msg userMsg = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(List.of(TextBlock.builder()
                        .text("你好！我的名字是 Alice。")
                        .build()))
                .build();

        Msg response = agent.call(userMsg).block();
        System.out.println("智能体: " + response.getTextContent());

        // 保存会话
        SessionManager.forSessionId(sessionId)
                .withSession(new JsonSession(sessionPath))
                .addComponent(agent)
                .saveSession();

        System.out.println("会话已保存到: " + sessionPath.resolve(sessionId + ".json"));
    }
}
