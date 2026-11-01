# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and make results visible. This
project targets JSON-RPC; TCK checks for transports and capabilities that are
not advertised by this integration must be reported separately from applicable
checks.

The repository includes `A2ASpringAbstractServerTest`, which extends the
official `AbstractA2AServerTest`. The class is disabled in the 0.1.x preview;
its presence does not mean the official contract runs or passes. Enabling and
passing it is a 0.2.0 conformance gate. Likewise, this preview does not claim
full TCK conformance. Push delivery can only be advertised when an application
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

The manually triggered [TCK workflow](../.github/workflows/tck.yml) runs the
Maven verification suite, starts the hello-world server, executes the official
JSON-RPC TCK, and uploads generated reports as workflow artifacts. It records
Java, Spring Boot, Maven, Python, A2A Java SDK, and TCK revision information.
The workflow is an available evidence mechanism; a run is evidence of a pass
only if it completed successfully and its report shows the applicable checks
passing. No TCK pass claim is made for 0.1.x by this document.

The TCK workflow is manually triggered. For 0.1.x, use it to collect preview
feedback and report unsupported or non-applicable capabilities separately.
For 0.2.0 conformance, link a successful workflow artifact after applicable
JSON-RPC checks and the official abstract server test pass. Never describe
skipped or untested checks as passing.
