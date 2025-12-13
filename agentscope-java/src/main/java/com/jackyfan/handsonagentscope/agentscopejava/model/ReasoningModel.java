package com.jackyfan.handsonagentscope.agentscopejava.model;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;

public class ReasoningModel {
    public static void main(String[] args) {
        GenerateOptions options = GenerateOptions.builder()
                // 思考的 token 预算
                .thinkingBudget(5000)
                .build();

        DashScopeChatModel reasoningModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-plus")
                .defaultOptions(options)
                .build();

        ReActAgent agent = ReActAgent.builder()
                .name("推理器")
                .model(reasoningModel)
                .build();
        System.exit(0);
    }
}
