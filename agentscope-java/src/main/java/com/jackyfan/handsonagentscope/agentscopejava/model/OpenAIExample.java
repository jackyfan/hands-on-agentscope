package com.jackyfan.handsonagentscope.agentscopejava.model;

import io.agentscope.core.message.*;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.OpenAIChatModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class OpenAIExample {
    public static void main(String[] args) {
        log.info(System.getenv("DEEPSEEK_API_KEY"));
        // 创建模型
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-chat")
                // 自定义端点
                .baseUrl("https://api.deepseek.com")
                .build();
        // 准备消息
        List<Msg> messages = List.of(
                Msg.builder()
                        .name("user")
                        .role(MsgRole.USER)
                        .content(List.of(TextBlock.builder().text("你好！").build()))
                        .build()
        );
        // 使用模型（与 DashScope 相同）
        model.stream(messages, null, null).flatMapIterable(ChatResponse::getContent)
                .map(block -> {
                    if(block instanceof TextBlock tb) {return tb.getText();}
                    if(block instanceof ThinkingBlock tb) {return tb.getThinking();}
                    if(block instanceof ToolUseBlock tub) {return tub.getContent();}
                    return "";
                }).filter(text -> !text.isEmpty())
                .doOnNext(System.out::print)
                .blockLast();
    }
}