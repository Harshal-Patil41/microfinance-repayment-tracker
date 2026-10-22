# Branch Policy

## Branches
| Branch | Purpose | Who merges into it |
|---|---|---|
| `main` | Stable, tagged releases only | Pull request from `develop` |
| `develop` | Integration branch (default) | Pull request from a feature branch |
| `feature/<short-name>` | One new feature or task | Created from `develop` |
| `fix/<short-name>` | Bug fix | Created from `develop` |
| `docs/<short-name>` | Documentation-only change | Created from `develop` |

## Naming rules
- Lowercase letters, digits and hyphens only, no spaces
- Keep it short and descriptive, for example `feature/add-borrower-form`
- One branch per piece of work; delete it after the merge

## Rules
1. Nobody commits directly to `main` or `develop`.
2. Every change goes through a pull request with a review.
3. The build (`mvn clean package`) must pass before merging.
4. Releases are tagged on `main`, for example `v1.0`.

## Commit messages
Prefix with `feat:`, `fix:`, `test:`, `docs:` or `chore:`, then a short description.
Example: `feat: add borrower entry form`