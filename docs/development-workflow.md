# Development workflow

This repository uses feature-based development on top of the official A2A Java SDK. `main` is the protected, release-facing branch. All changes go through pull requests.

## Start work

1. Open or find an issue and agree on the intended change.
2. Fork the repository and update your local `main`.
3. Create one short-lived branch for the issue, preferably named `issue-<number>-<short-description>`.

```bash
git switch main
git pull --ff-only origin main
git switch -c issue-123-short-description
```

Keep each pull request focused on one issue. Link the issue in the pull request description and commit message. Include tests and documentation for behavior changes.

## Commit and publish

```bash
git add <files>
git commit -m "feat: describe the change (#123)"
git push --set-upstream origin issue-123-short-description
```

Open a pull request targeting `main`. The Build and contribution-branch checks must pass, all review conversations must be resolved, and a repository maintainer must approve contributor-authored changes. Squash commits when merging unless multiple meaningful commits are useful.

## Main branch rules

The active `main-protection` ruleset targets `main` and requires pull requests, one approval, the `Build / build` and `Branch policy / Validate contribution branch` checks, an up-to-date branch, and resolved conversations. It blocks force pushes and branch deletion.

The repository currently has one maintainer, Soheil Qalamkari. Contributor-authored pull requests require the maintainer's approval. Maintainer-authored pull requests must still pass the same checks and use a pull request; because the author cannot approve their own change, the repository administrator may use the ruleset's pull-request-only bypass on a maintainer-authored pull request. The maintainer must still verify that all required checks pass. Never use the bypass for contributor pull requests.

The separate upstream A2A Java integration listing was accepted in [A2A Java PR #1201](https://github.com/a2aproject/a2a-java/pull/1201). This local policy governs this repository; it does not replace A2A Java's own contribution and review process.
