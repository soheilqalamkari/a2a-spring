# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and make results visible. This
project targets JSON-RPC; TCK checks for transports and capabilities that are
not advertised by this integration must be reported separately from applicable
checks.

The repository includes `A2ASpringAbstractServerTest`, which extends the
official `AbstractA2AServerTest`. At the current baseline, the class is
disabled; its presence does not mean the official contract runs or passes.
Enable it and verify the result in Maven/CI before claiming conformance. Push
notification delivery must also be implemented and tested before the
integration advertises that capability.

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

The manually triggered [TCK workflow](../.github/workflows/tck.yml) runs the
Maven verification suite, starts the hello-world server, executes the official
JSON-RPC TCK, and uploads the generated reports as workflow artifacts. The
workflow records Java, Spring Boot, Maven, Python, and A2A Java SDK versions.
It also records the TCK commit SHA checked out for that run, so the TCK source
can be reproduced even though the workflow clones the repository's current
default branch.

The TCK workflow is manually triggered. Release candidates must run it and
link a successful workflow artifact after the applicable JSON-RPC checks and
the official abstract server test pass. Report unsupported or non-applicable
capabilities explicitly; do not describe skipped or untested checks as
passing.
