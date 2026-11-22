# A2A Java SDK JPA Store Integration Evaluation

## Scope

This note evaluates whether the official A2A Java SDK `1.5.1.Final`
`JpaDatabaseTaskStore` can be used as the persistence implementation in a
Spring Boot application without running a CDI container or depending on
private SDK internals. It does not implement or advertise persistent storage.

The storage behavior and compatibility requirements remain subject to review
of the [task-store contract proposal](https://github.com/soheilqalamkari/a2a-spring/pull/49).
Implementation starts only after that contract is reviewed and merged, as
tracked by [issue #50](https://github.com/soheilqalamkari/a2a-spring/issues/50).

## Findings

The SDK extra is a Jakarta EE/CDI-managed store, not a container-neutral JPA
class that Spring can register as an ordinary bean:

- `JpaDatabaseTaskStore` is annotated with CDI `@ApplicationScoped`,
  `@Alternative`, and `@Priority`.
- It receives its `EntityManager` through `@PersistenceContext` for a named
  `a2a-java` persistence unit. Its README configures that unit as JTA.
- It injects CDI `Event<TaskFinalizedEvent>`, `A2AConfigProvider`, and
  `Instance<TaskAuthorizationProvider>`. Its constructor calls the SDK's
  `CdiUtils.getIfResolvable`, and initialization reads configuration in
  `@PostConstruct`.
- Store operations use Jakarta `@Transactional`. The module depends on CDI,
  Jakarta Inject, Persistence, and Transaction APIs and documents Quarkus and
  Jakarta EE deployment, not Spring Boot auto-configuration.

Spring Boot can manage JPA and transactions, but that alone does not provide
the CDI event bus, CDI `Instance` resolution, CDI alternative selection, or
the SDK's CDI initialization path. Constructing the class directly would
leave required private injected fields unset. Integrating a CDI container
could supply those services, but would add a second component model and
require a documented CDI/JTA deployment configuration; it is not a
Spring-native drop-in.

## Outcome

The pinned SDK JPA store cannot be reused as-is in the Spring integration
without CDI support. This finding does not rule out reuse of individual public
SDK entity or serialization types, and does not establish that a Spring-owned
store should duplicate the full implementation. Those choices need to be
checked against the reviewed task-store contract and coordinated before
implementation.

The next design decision is to compare the smallest maintainable options:

1. Ask the SDK to expose a container-neutral persistence core or supported
   Spring integration point, if that avoids duplicating protocol-facing store
   behavior.
2. If no suitable SDK integration point exists, implement a separate
   Spring-managed adapter against the public `TaskStore` and
   `TaskStateProvider` contracts, using Spring transactions and preserving
   the reviewed lifecycle, authorization, concurrency, and event guarantees.

Any adapter remains optional and separate from the web starter. In-memory
storage remains the default. Schema migration, task serialization upgrades,
restart behavior, and the boundary between persisted task records and
non-persisted queues, event-bus state, and SSE connections must be explicit
before storage is advertised.

## References

- [Pinned `JpaDatabaseTaskStore` source](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/extras/task-store-database-jpa/src/main/java/org/a2aproject/sdk/extras/taskstore/database/jpa/JpaDatabaseTaskStore.java)
- [Pinned JPA extra README](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/extras/task-store-database-jpa/README.md)
- [Pinned JPA extra Maven module](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/extras/task-store-database-jpa/pom.xml)
- [Spring task-store contract proposal, PR #49](https://github.com/soheilqalamkari/a2a-spring/pull/49)
- [Spring JPA adapter issue #50](https://github.com/soheilqalamkari/a2a-spring/issues/50)
