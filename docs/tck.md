# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and make results visible. This
project targets JSON-RPC; TCK checks for transports and capabilities that are
not advertised by this integration must be reported separately from applicable
checks.

The repository includes `A2ASpringAbstractServerTest`, which extends the
official `AbstractA2AServerTest`. It is enabled and passes all 54 cases with
zero failures, errors, or skips. Push delivery can only be advertised when an
application provides a sender and explicitly enables it.

## Local verification command

The TCK requires Python 3.11+ and `uv`:

```bash
mvn --batch-mode --no-transfer-progress -pl samples/hello-world -am -DskipTests package
java -jar samples/hello-world/target/a2a-spring-sample-hello-world-0.1.0-SNAPSHOT.jar --server.port=8080
```

With the sample server running, use another terminal:

```bash
git clone https://github.com/a2aproject/a2a-tck.git ../a2a-tck
cd ../a2a-tck
uv venv
source .venv/bin/activate
uv pip install -e .
./run_tck.py --sut-host http://localhost:8080 --transport jsonrpc --level must
```

The TCK writes evidence to `reports/compatibility.json`,
`reports/compatibility.html`, `reports/tck_report.html`, and
`reports/junitreport.xml`. Historical local summaries are intentionally not
presented as current conformance evidence because their totals describe
different report views and need reconciliation against a reproducible run.
A release candidate or upstream contribution must link the exact CI run and
report artifact used for its claims.

## CI verification

The [TCK workflow](../.github/workflows/tck.yml) runs the
Maven verification suite, starts the hello-world server, executes the official
JSON-RPC TCK, and uploads generated reports as workflow artifacts. It records
Java, Spring Boot, Maven, Python, A2A Java SDK, and TCK revision information.
### Latest CI evidence

The [A2A TCK workflow run](https://github.com/soheilqalamkari/a2a-spring/actions/runs/41416387895)
completed successfully for PR head `49dae10b1742f4146662522080307391407c8205`.
Download the [TCK evidence artifact](https://github.com/soheilqalamkari/a2a-spring/actions/runs/41416387895/artifacts/13127044413)
for JSON, HTML, and JUnit reports. It used A2A Java SDK `1.4.0.Final`,
Spring Boot `3.5.6`, Java `17.0.21`, Maven `3.9.15`, Python `3.11.17`, and
TCK commit `d4d6fae7887f88ef607fb461c13e3b0e1802f19b`.

The JUnit report contains 235 cases: 80 passed, 155 skipped, 0 failures, and
0 errors. The compatibility report records 67 MUST requirements as PASS, 25
as SKIPPED, and 22 as NOT TESTED. It reports 75.3% MUST compatibility and
64.4% overall compatibility. Skips include unsupported HTTP+JSON/gRPC
transports and capability-dependent checks, including push notifications
which the sample does not advertise. NOT TESTED items are not passes. This is
a successful MUST-level JSON-RPC workflow run, not a claim that every TCK
requirement is implemented or that full protocol conformance is complete.

### Review of skipped and untested checks

The 25 requirement-level skips break down as follows:

| TCK entries | Classification |
| --- | --- |
| `HTTP_JSON-ERR-001`, `HTTP_JSON-ERR-002`, `HTTP_JSON-SVC-002`, `HTTP_JSON-SSE-001`, `HTTP_JSON-STATUS-001`; `GRPC-ERR-001`, `GRPC-ERR-002`, `GRPC-ERR-003`, `GRPC-META-001`, `GRPC-SVC-001`, `GRPC-SVC-002` | Out of the advertised Spring MVC JSON-RPC/SSE transport scope. |
| `PUSH-CREATE-001/002`, `PUSH-GET-001/002`, `PUSH-LIST-001`, `PUSH-DEL-001/002`, `PUSH-DELIVER-001/002/003` | The TCK sample does not advertise push notifications. These checks do not establish push behavior; delivery remains application supplied and disabled in the sample. |
| `CARD-EXT-001/002` | The TCK sample does not advertise the extended Agent Card capability, so authentication and not-configured probes are not applicable to this sample. |
| `CORE-CAP-004` | The sample does not declare the TCK's required extension, so the missing-required-extension probe is skipped. Extension error behavior remains unverified by this run. |
| `CORE-CAP-002` | The TCK's negative probe asks a streaming-capable agent to behave as if streaming were unsupported. It is skipped because the sample advertises streaming; positive streaming checks are the relevant behavior. |

The JUnit report also contains skipped test cases beyond the 25
requirement-level entries. Most are parameterized checks for HTTP+JSON/gRPC or
capabilities the sample does not advertise. One JSON-RPC case was a genuine
gap: `test_returns_message_with_text_part[jsonrpc]` was skipped after the
sample agent threw `Cannot update task status - terminal state already
reached`, and the request timed out. The sample's message-only response path
emitted a message and then redundantly completed the task. That path has been
corrected in the PR branch to emit the expected direct message and return;
the updated TCK run must confirm this case passes before treating it as
resolved.

The 22 MUST requirements marked NOT TESTED are grouped here. These are not
passes, and they prevent a full conformance claim:

| Entries | Scope assessment |
| --- | --- |
| `CARD-SIGN-001`–`CARD-SIGN-004` | Agent Card JCS/JWS signing is not implemented or advertised; signing behavior remains unverified. |
| `AUTH-TLS-001`, `AUTH-SERVER-002`, `AUTH-INTASK-001`–`AUTH-INTASK-004`, `AUTH-SCOPE-001`–`AUTH-SCOPE-003` | Security and authorization are not provided by this starter. Production TLS and request authentication must be supplied by deployment/application configuration; in-task authorization behavior remains unverified. |
| `VER-CLIENT-001`, `VER-SERVER-001`, `VER-CLIENT-002` | Request-version signaling and version negotiation semantics were not tested. The report separately marks unsupported-version and empty-version probes as passing, but that does not cover these entries. |
| `BIND-EQUIV-001`–`BIND-EQUIV-004` | Multi-binding equivalence is not applicable while JSON-RPC is the only supported transport. |
| `HTTP_JSON-SVC-001`, `GRPC-SVC-003` | Binding-specific requirements for unsupported transports; out of current scope. |

The remaining 15 NOT TESTED entries are SHOULD/MAY requirements: task-history
and multi-turn behaviors (`CORE-HIST-*`, `CORE-MULTI-*`), serialization
forward compatibility (`DM-SERIAL-005`), Agent Card caching
(`CARD-CACHE-001`–`003`), and additional TLS/in-task-auth recommendations
(`AUTH-TLS-002`, `AUTH-SERVER-001`, `AUTH-INTASK-005/006`). Task history is
covered in part by the Java server contract suite, but this TCK run provides
no evidence for those history requirements.

For merge review, the protocol-relevant items still needing explicit follow-up
are the corrected JSON-RPC message-only case, version negotiation, security
and in-task authorization scope, and the extension/push/extended-card
capabilities if the project later advertises them. Unsupported transport and
multi-binding checks can remain out of scope while the Agent Card advertises
only JSON-RPC.

The workflow runs for pull requests, updates to `main`, and manual dispatch.
Report unsupported and non-applicable capabilities separately. Never describe
skipped or untested checks as passing.
