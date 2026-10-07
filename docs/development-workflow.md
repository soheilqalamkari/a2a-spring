# Development workflow

This repository uses feature-based development. `main` is the protected,
release-facing branch; work is merged into it through pull requests.

## Start work

Update the local main branch before creating a branch:

```bash
git switch main
git pull --ff-only origin main
git switch -c feature/short-description
```

Use an approved prefix: `feature/`, `fix/`, `docs/`, `chore/`, `refactor/`,
`test/`, `build/`, `ci/`, `perf/`, or `release/`.

## Commit and publish

```bash
git add <files>
git commit -m "feat: describe the change"
git push --set-upstream origin feature/short-description
```

Open a pull request from the feature branch to `main`. The branch policy
workflow validates the name, and the Build workflow must pass before merge.

## Repository protection

GitHub repository settings should require the following for `main`:

- pull requests instead of direct pushes;
- at least one approving review;
- the `Build / build` and `Branch policy / Validate feature branch name` checks;
- conversation resolution;
- an up-to-date branch before merging;
- no force pushes or branch deletion.

The GitHub settings are administrative state and must be enabled by a
repository administrator; the workflow in this repository provides the
version-controlled part of the policy.
