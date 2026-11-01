# Contributing

This is a community-maintained Spring Boot integration for the official A2A Java SDK. It is listed in the SDK's [server integrations](https://github.com/a2aproject/a2a-java/blob/main/docs/content/dev/server.md#server-integrations) following the acceptance of [A2A Java PR #1201](https://github.com/a2aproject/a2a-java/pull/1201). The listing does not make this repository part of the A2A organization or SDK codebase, and the A2A maintainers do not maintain this repository.

Contributions are welcome through issues and pull requests. The project follows the A2A Java project's contribution practices where they apply here: focused changes, an issue for the work, tests, documentation, and review before merge.

## Development workflow

- Open or find an issue before starting substantial work.
- Fork the repository and create one short-lived branch per issue. Use `issue-<number>-<short-description>` (for example, `issue-123-task-history`).
- Keep changes focused. Preserve protocol behavior in the official A2A Java SDK and keep Spring-specific integration behavior in this repository.
- Add or update tests and documentation for behavior changes. Run `mvn --batch-mode --no-transfer-progress verify` and include relevant results in the pull request.
- Link the issue in the pull request description and commit message. Use clear conventional commit messages.
- Squash commits when merging unless several commits represent useful, independently understandable steps.

## Review and merge policy

All contributions to `main` must be submitted as pull requests. The repository maintainer, Soheil Qalamkari, reviews and approves contributor-authored pull requests. Authors must not approve their own pull requests. Do not merge until required checks pass and review conversations are resolved.

The maintainer's own pull requests also use the same branch and pull request workflow and must pass all required checks. Because the maintainer cannot review their own work, GitHub repository rules should allow the repository administrator to bypass only the required approval for a maintainer-authored pull request; this bypass must not be used for contributor pull requests.

## Compatibility and scope

Include a compatibility note when changing the supported A2A Java SDK, Java, or Spring Boot versions. Keep unsupported capabilities and known limitations explicit in the documentation. Do not describe this community project as maintained by or officially endorsed by the A2A project.
