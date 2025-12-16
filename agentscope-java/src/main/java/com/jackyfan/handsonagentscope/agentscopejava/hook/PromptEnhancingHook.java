package com.jackyfan.handsonagentscope.agentscopejava.hook;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreReasoningEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class PromptEnhancingHook implements Hook {
    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreReasoningEvent e) {
            List<Msg> messages = new ArrayList<>(e.getInputMessages());
            messages.add(0, Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .content(List.of(TextBlock.builder().text("逐步思考。").build()))
                    .build());
            e.setInputMessages(messages);
            return Mono.just(event);
        }
        return Mono.just(event);
    }
}
