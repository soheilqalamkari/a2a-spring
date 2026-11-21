package io.a2aspring.web;

import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.server.auth.User;

/** Resolves the SDK user for a servlet request. */
@FunctionalInterface
public interface ServerCallContextUserResolver {
    User resolve(HttpServletRequest request);
}
