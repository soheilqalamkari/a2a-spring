package io.a2aspring.web;

import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.server.ServerCallContext;

/** Creates the official SDK call context from an incoming Spring web request. */
@FunctionalInterface
public interface ServerCallContextFactory {
    ServerCallContext create(HttpServletRequest request);
}
