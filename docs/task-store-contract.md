# Persistent task-store contract

This document defines the compatibility boundary for a future persistent task
store in A2A Spring. It is a design gate for the 0.3.0 roadmap, not a claim
that this repository currently provides durable storage.

The pinned API is A2A Java SDK `1.5.1.Final`:

- [`TaskStore`](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/server-common/src/main/java/org/a2aproject/sdk/server/tasks/TaskStore.java)
- [`TaskStateProvider`](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/server-common/src/main/java/org/a2aproject/sdk/server/tasks/TaskStateProvider.java)
- [`InMemoryTaskStore`](https://github.com/a2aproject/a2a-java/blob/v1.5.1.Final/server-common/src/main/java/org/a2aproject/sdk/server/tasks/InMemoryTaskStore.java)

Protocol behavior remains in the official SDK. A Spring storage module may
provide persistence and Spring Data integration, but it must implement the SDK
contracts without changing request or task semantics.

## SDK contract

### Saving task state

`TaskStore.save(Task, boolean)` receives the complete task snapshot and an
`isReplicated` flag. The flag identifies an update originating from a
replicated event; it is not a “new task” or “terminal task” flag. The SDK
documents saves after status updates and artifact changes, synchronously
before events are distributed and before responses are returned. The adapter
must complete persistence before returning from `save` and propagate storage
failures through the SDK's `TaskStoreException` hierarchy.

Persist the fields supported by the SDK `Task`: identifier, context, status
and timestamp, artifacts, message history, and metadata. Treat each save as a
coherent update. A database transaction should prevent a reader from seeing a
new status with stale history or artifacts from the preceding snapshot.

The SDK requires thread-safe store operations. Concurrent updates to different
tasks must be supported. The SDK does not prescribe same-task conflict
resolution; the adapter must document whether it serializes updates, uses
optimistic locking, or follows another explicit rule. Silent stale overwrites
must not be presented as conflict detection.

### Reads and deletion

`get(taskId)` returns the task or `null` when no record exists. `delete(taskId)`
removes the task according to the adapter's documented policy. If deletion is
logical or delayed by retention, subsequent reads and lists must still have
well-defined behavior and must not expose a deleted task as an ordinary live
record.

Persistence of task records does not persist the SDK queue manager, event bus,
push-notification delivery, or live SSE subscriptions. An adapter must not
claim those facilities survive restarts unless they receive separate durable
implementations.

### List behavior and authorization

`list(ListTasksParams, ServerCallContext)` must preserve the SDK's filters,
pagination, result counts, history limit, and artifact inclusion behavior.
For SDK `1.5.1.Final`, the in-memory reference uses context, status, and
status-timestamp filters; sorts by status timestamp descending and task ID
ascending; and uses a timestamp/task-ID page token. It trims returned history
to the requested length and omits artifacts unless requested.

When task authorization is configured, per-task read authorization must be
applied before pagination. The SDK's `listAuthorized` contract requires an
exact `totalSize` across all pages for the authorized result set. Its fallback
may scan candidates repeatedly and does not promise a shared database snapshot
while tasks or authorization decisions change concurrently. An adapter that
overrides this method must document its consistency boundary and preserve the
authorization and count contract.

The SDK `Task` model has no tenant field. Although `ListTasksParams` contains a
tenant value, the `1.5.1.Final` in-memory store does not use it as an isolation
filter. The first persistent adapter must not claim tenant isolation until it
defines a trusted tenant source and applies it consistently to get, list,
delete, and authorization operations. Task IDs must not accidentally become
cross-tenant access paths.

### Active and finalized state

`TaskStateProvider` exposes two different checks:

- `isTaskActive` includes tasks that are not final and tasks finalized within
  the SDK's configured queue grace period.
- `isTaskFinalized` reports a final task immediately and ignores that grace
  period.

The adapter must keep these checks consistent with its persisted task status
and the grace-period behavior used by the SDK queue manager. A missing task is
neither active nor finalized.

Persisted non-final status is not proof that an agent is still executing after
a process restart. The first adapter must state whether it supports execution
recovery. Unless a separate recovery mechanism is implemented and tested, it
must not claim that in-flight work resumes automatically. The queue-manager
behavior for such tasks must be documented before the adapter is advertised.

## Spring module boundary

The adapter belongs in an optional module or starter. The existing web starter
must retain the SDK's in-memory store as its default and must not acquire a
database driver, ORM, or Spring Data dependency merely because the web starter
is present. Applications must be able to supply their own `TaskStore` and
matching `TaskStateProvider` beans.

Before choosing an implementation, evaluate whether the official SDK's JPA
task-store extra can be used with Spring Boot's persistence lifecycle or
whether a Spring Data adapter is needed. Avoid duplicating protocol or task
serialization behavior that the official SDK already provides.

## Transaction and failure boundary

The task snapshot should commit before the SDK distributes the corresponding
event or returns a response. If persistence commits but event publication
fails, the task record may be ahead of connected subscribers; the initial
adapter must document that limitation. A transactional outbox or durable event
delivery is a separate capability and must not be implied by task persistence.

Serialization failures and database failures should surface as the SDK's
appropriate task-store exceptions with the original cause retained. The
adapter must not report a successful save after a failed commit.

## Acceptance tests for a future adapter

In addition to the official `AbstractA2AServerTest` contract, the adapter needs
storage-specific coverage for:

- round-trip persistence of task status, timestamps, artifacts, history, and
  metadata;
- save/update/delete and missing-task behavior;
- pagination, filters, history limits, artifact inclusion, stable page tokens,
  and authorization before pagination;
- active/finalized behavior around terminal-state grace periods;
- concurrent writes to one task and to different tasks;
- database failure and serialization failure propagation;
- application restart behavior, including the declared policy for non-final
  tasks and the fact that queues and SSE subscriptions are not durable.

The default remains in-memory storage until a specific adapter meets these
gates and documents database setup, migration ownership, retention, and
backup responsibilities.
