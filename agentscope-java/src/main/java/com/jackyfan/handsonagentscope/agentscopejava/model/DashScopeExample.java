package com.jackyfan.handsonagentscope.agentscopejava.model;

import io.agentscope.core.message.*;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.DashScopeChatModel;

import java.util.List;

public class DashScopeExample {
    public static void main(String[] args) {
        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-plus")
                .build();

        // 准备消息
        List<Msg> messages = List.of(
                Msg.builder()
                        .name("user")
                        .role(MsgRole.USER)
                        .content(List.of(TextBlock.builder().text("你好！").build()))
                        .build()
        );
        //使用模型
        model.stream(messages, null, null).flatMapIterable(ChatResponse::getContent)
                .map(block -> {
                    if (block instanceof TextBlock tb) return tb.getText();
                    if (block instanceof ThinkingBlock tb) return tb.getThinking();
                    if (block instanceof ToolUseBlock tub) return tub.getContent();
                    return "";
                }).filter(text -> !text.isEmpty())
                .doOnNext(System.out::print)
                .blockLast();
        System.exit(0);
    }
}
