package com.jackyfan.handsonagentscope.agentscopejava.model.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.StructuredOutputReminder;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class ComprehensiveAgentExample {
    public static class WeatherTools{
        @Tool(description = "获取指定城市的天气信息")
        public static String getWeather(@ToolParam(name = "city", description = "要查询天气的城市名称") String city,
                                        UserContext userContext) {
            return String.format("用户 %s 查询：%s 的天气为晴天，25°C",
                    userContext.getUserId(), city);
        }
    }

    public static class CalculatorTools{
        @Tool(description = "计算两个数字的和")
        public static double add(@ToolParam(name = "a", description = "第一个数字") double a,
                                 @ToolParam(name = "b", description = "第二个数字") double b){
            return a + b;
        }
    }


    public static class UserContext {
        private final String userId;
        private final String role;

        public UserContext(String userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getRole() { return role; }
    }

    // 3. 定义自定义 Hook
    public static class LoggingHook implements Hook {
        @Override
        public Mono<HookEvent> onEvent(HookEvent event) {
            System.out.println("[Hook] 事件: " + event.getType() +
                    ", Agent: " + event.getAgent().getName());
            return Mono.just(event);
        }
    }

    // 4. 定义结构化输出的数据类
    public record CityWeather (String city, String weather, Integer temperature){}

    public static void main(String[] args) {
        // ============================================================
        // 第一步：配置工具和工具组
        // ============================================================

        Toolkit toolkit = new Toolkit();

        // 创建工具组
        toolkit.createToolGroup("basic", "基础工具组", true);
        toolkit.createToolGroup("advanced", "高级工具组", false);

        // 注册工具到不同组
        toolkit.registration()
                .tool(new WeatherTools())
                .group("basic")
                .apply();

        toolkit.registration()
                .tool(new CalculatorTools())
                .group("advanced")
                .apply();

        // ============================================================
        // 第二步：配置工具执行上下文
        // ============================================================

        ToolExecutionContext toolContext = ToolExecutionContext.builder()
                .register(new UserContext("user-abc-123", "admin"))
                .build();

        // ============================================================
        // 第三步：配置执行策略（超时和重试）
        // ============================================================

        // 模型调用执行配置
        ExecutionConfig modelExecutionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofMinutes(3))
                .maxAttempts(5)
                .initialBackoff(Duration.ofSeconds(2))
                .maxBackoff(Duration.ofSeconds(30))
                .backoffMultiplier(2.0)
                .retryOn(error -> {
                    String msg = error.getMessage();
                    return msg != null && (msg.contains("timeout")
                            || msg.contains("rate limit")
                            || msg.contains("503"));
                })
                .build();

        // 工具执行配置
        ExecutionConfig toolExecutionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(60))
                .maxAttempts(1)
                .build();


        // ============================================================
        // 第四步：配置 Hook
        // ============================================================

        List<Hook> hooks = List.of(
                new LoggingHook()
                // 可添加更多 Hook，如 StudioMessageHook
        );

        // ============================================================
        // 第六步：配置 Agent（完整配置）
        // ============================================================

        ReActAgent agent = ReActAgent.builder()
                // 1. 基础配置
                .name("智能助手")
                .sysPrompt("""
                你是一个智能助手，拥有以下能力：
                - 查询天气信息
                - 进行数学计算
                
                职责：
                - 准确理解用户需求
                - 合理使用工具完成任务
                - 用简洁的语言回答
                
                限制：
                - 不要编造信息
                - 不确定时请诚实告知
                """)

                // 2. 模型配置
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName("qwen-max")
                        .stream(true)
                        .enableThinking(true)
                        .formatter(new DashScopeChatFormatter())
                        .defaultOptions(GenerateOptions.builder()
                                .temperature(0.7)
                                .maxTokens(2000)
                                .topP(0.9)
                                .thinkingBudget(1024)
                                .build())
                        .build())

                // 3. 记忆配置
                .memory(new InMemoryMemory())

                // 4. 工具配置
                .toolkit(toolkit)
                .toolExecutionContext(toolContext)

                // 5. 执行参数
                .maxIters(10)
                .modelExecutionConfig(modelExecutionConfig)
                .toolExecutionConfig(toolExecutionConfig)

                // 6. Hook 配置
                .hooks(hooks)

                // 7. 结构化输出
                .structuredOutputReminder(StructuredOutputReminder.TOOL_CHOICE)

                .build();

        // ============================================================
        // 第七步：使用 Agent
        // ============================================================

        try {
            // 示例 1: 基础对话
            System.out.println("=== 示例 1: 基础对话 ===");
            Msg msg1 = Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder()
                            .text("你好！请介绍一下你自己。")
                            .build())
                    .build();

            Msg response1 = agent.call(msg1).block();
            System.out.println("回答: " + response1.getTextContent() + "\n");

            // 示例 2: 工具调用（基础工具组已激活）
            System.out.println("=== 示例 2: 使用工具查询天气 ===");
            Msg msg2 = Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder()
                            .text("北京的天气怎么样？")
                            .build())
                    .build();

            Msg response2 = agent.call(msg2).block();
            System.out.println("回答: " + response2.getTextContent() + "\n");

            // 示例 3: 动态激活高级工具组
            System.out.println("=== 示例 3: 激活高级工具组并使用计算器 ===");
            toolkit.updateToolGroups(List.of("advanced"), true);

            Msg msg3 = Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder()
                            .text("计算 123.45 + 678.90 等于多少？")
                            .build())
                    .build();

            Msg response3 = agent.call(msg3).block();
            System.out.println("回答: " + response3.getTextContent() + "\n");

            // 示例 4: 结构化输出
            System.out.println("=== 示例 4: 结构化输出 ===");
            Msg msg4 = Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder()
                            .text("查询上海的天气，并以结构化格式返回城市名、天气状况、温度")
                            .build())
                    .build();

            Msg response4 = agent.call(msg4, CityWeather.class).block();
            CityWeather weatherData = response4.getStructuredData(CityWeather.class);

            System.out.println("提取的结构化数据:");
            System.out.println("  城市: " + weatherData.city);
            System.out.println("  天气: " + weatherData.weather);
            System.out.println("  温度: " + weatherData.temperature + "°C");
            System.out.println();
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
