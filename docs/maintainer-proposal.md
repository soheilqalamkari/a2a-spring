# Project status and relationship to A2A Java

The Spring integration was added to the A2A Java SDK's [server integrations list](https://github.com/a2aproject/a2a-java/blob/main/docs/content/dev/server.md#server-integrations) in [A2A Java PR #1201](https://github.com/a2aproject/a2a-java/pull/1201). That pull request was merged on 2026-10-28.

## Project ownership

A2A Spring is a community-maintained integration hosted in this repository. Its upstream listing helps users discover it; it does not transfer ownership to the A2A organization, make its code part of the A2A Java SDK, or mean that A2A maintainers maintain or endorse this repository.

The official A2A Java SDK remains the source of protocol implementation. This repository provides Spring Boot integration and maintains its own issues, pull requests, releases, and support.

## Current scope

The project targets Java 17+, Spring Boot 3.5.x, and A2A Java SDK 1.4.0.Final. It provides auto-configuration, Agent Card discovery, a Spring MVC JSON-RPC endpoint with SSE streaming, a runnable sample, and Java-client interoperability coverage. The official `AbstractA2AServerTest` passes all 54 cases on the current development branch. The JSON-RPC TCK has not yet been run successfully on this branch. See [compatibility](compatibility.md), [server behavior](server.md), and [TCK conformance](tck.md) for verified behavior and remaining work.

## Contributions

Contributions are welcome through issues and pull requests. See [CONTRIBUTING.md](../CONTRIBUTING.md) for the feature-branch workflow, tests, and maintainer review policy.
