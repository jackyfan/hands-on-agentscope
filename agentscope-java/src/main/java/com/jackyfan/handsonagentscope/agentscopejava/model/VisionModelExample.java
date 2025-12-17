package com.jackyfan.handsonagentscope.agentscopejava.model;

import io.agentscope.core.message.*;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.studio.StudioManager;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class VisionModelExample {
    public static void main(String[] args) throws IOException {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("agentscope-java")
                .runName("VisionModelExample")
                .initialize()
                .block();
        // 创建视觉模型
        DashScopeChatModel visionModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-vl-max")  // 视觉模型
                .build();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:/pics/coder.jpeg");
        String base64Image = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Paths.get(resource.getURI()))
        );
        // 准备多模态消息
        Msg imageMsg = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(List.of(
                        TextBlock.builder().text("这张图片里有什么？").build(),
                        ImageBlock.builder().source(URLSource.builder()
                                .url("https://s.abcnews.com/images/Business/GTY_convertible_kab_150716_16x9_992.jpg")
                                .build()).build()
                ))
                .build();

        // 生成响应
        visionModel.stream(List.of(imageMsg), null, null).flatMapIterable(ChatResponse::getContent).map(block -> {
            if (block instanceof TextBlock tb) return tb.getText();
            if (block instanceof ThinkingBlock tb) return tb.getThinking();
            if (block instanceof ToolUseBlock tub) return tub.getContent();
            return "";
        }).filter(text -> !text.isEmpty()).doOnNext(System.out::print).blockLast();
    }

}
