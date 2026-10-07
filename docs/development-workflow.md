# Development workflow

This repository uses feature-based development. `main` is the protected,
release-facing branch; work is merged into it through pull requests.

## Start work

Update the local main branch before creating a branch for the issue:

```bash
git switch main
git pull --ff-only origin main
git switch -c issue-123-short-description
```

Use one short-lived branch per issue. The preferred convention is the issue
number followed by a short description, for example `issue-123-spring-bean`.

## Commit and publish

```bash
git add <files>
git commit -m "feat: describe the change (#123)"
git push --set-upstream origin issue-123-short-description
```

Open a pull request from the issue branch to `main`, link the issue in the
description, and include tests and documentation. Squash the commits before
merge unless the change is large enough to justify multiple meaningful
commits. The branch policy and Build workflows must pass before merge.

## Repository protection

GitHub repository settings should require the following for `main`:

- pull requests instead of direct pushes;
- at least one approving review;
- the `Build / build` and `Branch policy / Validate contribution branch` checks;
- conversation resolution;
- an up-to-date branch before merging;
- no force pushes or branch deletion.

The GitHub settings are administrative state and must be enabled by a
repository administrator; the workflow in this repository provides the
version-controlled part of the policy.
