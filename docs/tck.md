# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and make results visible. This
project targets JSON-RPC; TCK checks for transports and capabilities that are
not advertised by this integration must be reported separately from applicable
checks.

The repository includes `A2ASpringAbstractServerTest`, which extends the
official `AbstractA2AServerTest`. It is enabled and currently passes all 54
cases with zero failures, errors, or skips on the development branch. This is
evidence for the SDK server-test contract; it does not establish full TCK
conformance. Push delivery can only be advertised when an application
provides a sender and explicitly enables it.

## Local verification command

The TCK requires Python 3.11+ and `uv`:

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
The workflow is evidence of a pass only if it completed successfully and its
report shows the applicable checks passing. The TCK has not yet been run on
this development branch, so no TCK pass claim is made here.

The workflow runs for pull requests, updates to `main`, and manual dispatch.
Report unsupported and non-applicable capabilities separately. Never describe
skipped or untested checks as passing.
