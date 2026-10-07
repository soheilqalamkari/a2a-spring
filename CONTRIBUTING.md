# Contributing

This project is intended to become a community Spring Boot integration for the A2A ecosystem.

Before opening an upstream proposal:

1. Keep protocol implementation in the official A2A Java SDK.
2. Keep Spring MVC, WebFlux, Security, and observability concerns in optional modules.
3. Add unit and interoperability tests for each transport.
4. Run `mvn verify` and include the result in the pull request.
5. Discuss the repository location and project naming with A2A maintainers before publication.

The attached project requirements document is planning material; it is not a
contributor instruction file and should not be treated as an upstream policy.

For ecosystem listing, follow the official A2A Java integration process and
include a project page, usage instructions, runnable sample, interoperability
tests, and evidence of the official SDK compatibility.

Use conventional commit messages and include a clear compatibility note when changing the supported A2A SDK version.

## Branch and pull request workflow

The `main` branch is release-facing and must not be used for direct
development. Create one short-lived branch for each issue and merge it
through a pull request.

Use the issue number followed by a short description:

```text
issue-123-short-description
```

Examples:

```bash
git switch -c issue-123-persistent-task-store
git switch -c issue-124-sse-stream-close
git switch -c issue-125-tck-evidence
```

Link the issue in both the pull request description and the commit message.
Include tests and appropriate documentation. Squash commits before merging
unless multiple meaningful commits are needed. Every pull request requires
review by at least one project committer.

Pull requests targeting `main` must pass the Build workflow and the
contribution-branch policy. Maintainers should enable branch protection
requiring pull requests, a successful Build check, an up-to-date branch, and
conversation resolution before merge.
