# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and to make the result visible.
The current MVP has official-client interoperability tests, including SSE
streaming. The latest local TCK run against the advertised JSON-RPC interface
passed 78 checks and skipped 15 unsupported or optional checks. The aggregate
report is 64.4% because the TCK also accounts for capabilities that this
integration does not advertise, including gRPC, HTTP+JSON, authentication,
card signing, and persistence/history scenarios. It is not yet a complete
upstream integration submission because the `AbstractA2AServerTest` scaffold
is disabled and push notification delivery is not implemented.

The project includes a compiled compatibility scaffold,
`A2ASpringAbstractServerTest`, based on the official
`a2a-java-sdk-tests-server-common` test JAR. It is disabled until the missing
server capabilities are implemented; it must not be presented as a passing TCK
result.

## Planned verification command

The TCK requires Python 3.11+ and `uv`:

```bash
git clone https://github.com/a2aproject/a2a-tck.git ../a2a-tck
cd ../a2a-tck
uv venv
source .venv/bin/activate
uv pip install -e .
./run_tck.py --sut-host http://localhost:8080 --transport jsonrpc --level must
```

The verified local command produced 80 passed pytest cases and the following
compatibility summary:

```text
JSON-RPC: 78/93 passed, 15 skipped
MUST:     67 passed, 25 not applicable/skipped, 22 not tested
```

The TCK writes evidence to `reports/compatibility.json`,
`reports/compatibility.html`, `reports/tck_report.html`, and
`reports/junitreport.xml`. A release or upstream pull request must link a
reproducible CI report after the disabled test-store and push-notification
capabilities are implemented.
