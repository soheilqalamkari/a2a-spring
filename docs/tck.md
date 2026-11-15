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

The pinned TCK revision requires Python 3.15+ and `uv`:

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

The [A2A TCK workflow run](https://github.com/soheilqalamkari/a2a-spring/actions/runs/43720151911)
completed successfully for PR #14. Download the
[TCK evidence artifact](https://github.com/soheilqalamkari/a2a-spring/actions/runs/43720151911/artifacts/14164123103)
for the separate `tck-baseline/` and `tck-required-extension/` reports. It
used A2A Java SDK `1.5.1.Final`, Spring Boot `3.5.6`, Java `17.0.21`,
Maven `3.9.15`, Python `3.15.2`, and TCK commit
`104a7b4ae0d83f65c075e3793e322953a830eaba`.

The baseline report records 66 MUST requirements as PASS, 25 as SKIPPED,
17 as NOT TESTED, and 7 as pending manual verification. Its JUnit report
records 78 passed, 151 skipped, 34 deselected, 0 failures, and 0 errors.
The separate scoped run selected the required-extension checks: 1 passed,
1 skipped, 261 deselected. It records `CORE-CAP-004` as PASS for JSON-RPC
and SKIPPED for HTTP+JSON. The scoped result does not change the baseline
counts or establish full TCK conformance.
### Review of skipped and untested checks

The 25 requirement-level skips break down as follows:

| TCK entries | Classification |
| --- | --- |
| `HTTP_JSON-ERR-001`, `HTTP_JSON-ERR-002`, `HTTP_JSON-SVC-002`, `HTTP_JSON-SSE-001`, `HTTP_JSON-STATUS-001`; `GRPC-ERR-001`, `GRPC-ERR-002`, `GRPC-ERR-003`, `GRPC-META-001`, `GRPC-SVC-001`, `GRPC-SVC-002` | Out of the advertised Spring MVC JSON-RPC/SSE transport scope. |
| `PUSH-CREATE-001/002`, `PUSH-GET-001/002`, `PUSH-LIST-001`, `PUSH-DEL-001/002`, `PUSH-DELIVER-001/002/003` | The TCK sample does not advertise push notifications. These checks do not establish push behavior; delivery remains application supplied and disabled in the sample. |
| `CARD-EXT-001/002` | The TCK sample does not advertise the extended Agent Card capability, so authentication and not-configured probes are not applicable to this sample. |
| `CORE-CAP-004` | The baseline sample does not declare the TCK's required extension, so this baseline probe is skipped. A separate `tck-required-extension` profile and scoped CI run exercise the required-extension error; see the scoped check below. The baseline count remains unchanged. |
| `CORE-CAP-002` | The TCK's negative probe asks a streaming-capable agent to behave as if streaming were unsupported. It is skipped because the sample advertises streaming; positive streaming checks are the relevant behavior. |

### Scoped required-extension check (`CORE-CAP-004`)

The CI workflow runs a second hello-world server with the
`tck-required-extension` profile. This profile advertises the TCK sentinel
extension `urn:a2a:tck:required-extension` as required. The workflow runs only
the official `TestCapabilityExtensionRequired` JSON-RPC check and stores its
reports under `tck-required-extension/`, separate from the baseline report
under `tck-baseline/`. CI fails if the test does not report
`CORE-CAP-004` as `PASS` for JSON-RPC. The default Agent Card remains
extension-free, and this scoped result does not alter the baseline TCK totals
or imply full TCK conformance.

To reproduce the scoped check locally, build and start the sample from the
repository root:

```bash
mvn --batch-mode --no-transfer-progress -pl samples/hello-world -am -DskipTests package
java -jar samples/hello-world/target/a2a-spring-sample-hello-world-0.1.0-SNAPSHOT.jar \
  --spring.profiles.active=tck-required-extension \
  --server.port=8081 \
  --a2a.agent.url=http://localhost:8081/a2a
```

Then, from the TCK checkout:

```bash
./run_tck.py --sut-host http://localhost:8081 --transport jsonrpc --level must \
  -- -k TestCapabilityExtensionRequired
```

The scoped evidence is valid only when `compatibility.json` reports both
`per_requirement.CORE-CAP-004.status` and
`per_requirement.CORE-CAP-004.transports.jsonrpc` as `PASS`.

The JUnit report also contains skipped test cases beyond the 25
requirement-level entries. Most are parameterized checks for HTTP+JSON/gRPC or
capabilities the sample does not advertise. An earlier run skipped the
in-scope JSON-RPC case `test_returns_message_with_text_part[jsonrpc]`: the
sample had submitted a task before returning a message, then redundantly
completed the terminal task and timed out. The sample now handles this
message-only response before task creation. In the latest run, this test
passes; the remaining 151 skipped JUnit cases are classified by the reasons
above or by the untested requirements below.

The latest report marks 17 MUST requirements NOT TESTED. These are not
passes, and they prevent a full conformance claim:

| Entries | Scope assessment |
| --- | --- |
| `AUTH-SERVER-002`, `AUTH-INTASK-001`–`AUTH-INTASK-004`, `AUTH-SCOPE-001`–`AUTH-SCOPE-003` | These checks need an agent that declares and exercises authentication/authorization requirements. The sample declares no security schemes; in-task authorization behavior remains unverified. |
| `VER-CLIENT-001`, `VER-CLIENT-002` | Client-side requirements; this server integration does not implement an A2A client. |
| `VER-SERVER-001` | The TCK does not test this server requirement. Focused Spring JSON-RPC integration tests against A2A Java SDK `1.5.1.Final` cover advertised `1.0`, rejection of unsupported `1.1` and `2.0`, acceptance of patch version `1.0.7`, and absent/empty defaults. SDK `1.5.1.Final` includes the fix merged for [A2A Java issue #1294](https://github.com/a2aproject/a2a-java/issues/1294). This requirement is supported by focused server tests, not by a TCK PASS; see the latest CI evidence above. |
| `BIND-EQUIV-001`–`BIND-EQUIV-004` | Multi-binding equivalence is not applicable while JSON-RPC is the only supported transport. |
| `HTTP_JSON-SVC-001`, `GRPC-SVC-003` | Binding-specific requirements for unsupported transports; out of current scope. |

Seven MUST requirements are pending manual verification rather than marked
NOT TESTED: `CARD-CLIENT-001`, `CARD-SIGN-001`–`CARD-SIGN-004`,
`AUTH-TLS-001`, and `BIND-CUSTOM-002`. This project does not advertise Agent
Card signing. The TCK also lists `AUTH-TLS-002` and `AUTH-SERVER-001` for
manual verification at SHOULD level.

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
