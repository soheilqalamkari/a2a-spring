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

The workflow runs for pull requests, updates to `main`, and manual dispatch.
Report unsupported and non-applicable capabilities separately. Never describe
skipped or untested checks as passing.
