package com.jackyfan.handsonagentscope.agentscopejava.hook;

import io.agentscope.core.hook.ErrorEvent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import reactor.core.publisher.Mono;

public class ErrorHandlingHook implements Hook {
    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {

        if (event instanceof ErrorEvent e) {
            System.err.println("智能体错误: " + e.getAgent().getName());
            System.err.println("错误消息: " + e.getError().getMessage());
            return Mono.just(event);
        }

        return Mono.just(event);
    }
}
