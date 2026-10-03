# Security policy

Do not report security vulnerabilities in public issues. Send a private report
to the repository maintainers with the affected version, reproduction steps,
and any relevant logs or proof of concept.

The current MVP does not provide authentication or authorization. Deployments
that need access control must place the endpoint behind an appropriate Spring
Security or network policy until the dedicated security module is available.
