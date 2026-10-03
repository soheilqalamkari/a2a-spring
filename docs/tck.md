# A2A conformance testing

The official A2A integration guidance asks integrations to pass the [A2A
TCK](https://github.com/a2aproject/a2a-tck) and to make the result visible.
The current MVP has official-client interoperability tests, including SSE
streaming, but is not yet TCK-conformant: it does not expose the complete push
notification delivery and test-store surface required by the upstream
`AbstractA2AServerTest`.

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

The TCK writes its evidence to `reports/compatibility.json`,
`reports/compatibility.html`, `reports/tck_report.html`, and
`reports/junitreport.xml`. A release or upstream pull request must link the
report from a reproducible CI run after the currently disabled capabilities are
implemented.
