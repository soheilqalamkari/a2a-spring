# Proposal: Spring Boot integration for the official A2A Java SDK

## Summary

This repository provides a Spring Boot integration for the official A2A Java
SDK. It keeps protocol models and request handling in the SDK and adds Spring
Boot auto-configuration, `@ConfigurationProperties`, Spring bean discovery,
and a Spring MVC JSON-RPC adapter.

## Motivation

Spring Boot is widely used for Java services, but Spring applications currently
need to understand SDK-specific server bootstrap and CDI-oriented wiring to
expose an A2A agent. This integration makes the basic path a normal Spring
application: add a starter, register an `AgentExecutor` bean, and configure an
Agent Card.

## Current MVP

- official A2A Java SDK `1.4.0.Final` as the protocol implementation;
- Java 17 and Spring Boot 3.5 baseline;
- typed `a2a.*` properties and Spring `Environment` configuration;
- conditional Boot auto-configuration and executor discovery;
- Agent Card endpoint at `/.well-known/agent-card.json`;
- Spring MVC JSON-RPC endpoint at `/a2a`;
- support for both the protocol-style `message/send` and the official Java
  client's `SendMessage` operation name;
- runnable hello-world sample;
- Spring context, endpoint, auto-configuration, and official-client HTTP tests;
- source/Javadoc/signing/Central Portal release preparation.

## Scope boundaries

The MVP intentionally does not claim WebFlux, authentication, persistence,
push notification delivery, observability, or Spring AI support.
Those can be added as separate modules after maintainers agree on the API and
compatibility boundary.

## Hosting question

Please advise whether the integration should remain an independent community
repository or move under the A2A organization. The current local coordinates
and SCM metadata use `github.com/soheilghalamkari/a2a-spring` and
`io.github.soheilghalamkari` provisionally; they should be changed if the
maintainers prefer another repository or namespace.

## Evidence

The complete Maven reactor passes with the official client interoperability
test enabled. The sample and setup instructions are in the README and
`docs/getting-started.md`.
