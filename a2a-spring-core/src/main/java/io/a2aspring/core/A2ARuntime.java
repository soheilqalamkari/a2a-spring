package io.a2aspring.core;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.config.A2AConfigProvider;
import org.a2aproject.sdk.spec.AgentCard;

/** Immutable integration boundary used by transport adapters and applications. */
public record A2ARuntime(
        A2AProperties properties,
        A2AConfigProvider configProvider,
        AgentExecutor agentExecutor,
        AgentCard agentCard) {
}
