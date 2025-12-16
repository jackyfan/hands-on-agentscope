package com.jackyfan.handsonagentscope.agentscopejava.hook;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import reactor.core.publisher.Mono;

public class HighPriorityHook implements Hook {
    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        //此钩子在优先级 > 10 的钩子之前执行
        return Mono.just(event);
    }

    @Override
    public int priority() {
        return 10;
    }
}
