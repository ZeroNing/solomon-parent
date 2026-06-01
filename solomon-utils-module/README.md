# Solomon Utils Module

`solomon-utils-module` groups reusable utilities by responsibility. New modules should depend on the
smallest library that provides the required API.

| Module | Responsibility |
| --- | --- |
| `solomon-utils-core` | Project-specific validation, enum helpers, file helpers and lambdas |
| `solomon-utils-spring` | Spring container access, startup templates and exception helpers |
| `solomon-utils-json` | Jackson configuration, serializers and JSON annotations |
| `solomon-utils-algorithm` | Optional sorting algorithms |
| `solomon-epc-coder` | GS1 EPC encoding and decoding |
| `solomon-bot-notice` | DingTalk, WeChat Work and Feishu notifications |
| `solomon-clamav` | ClamAV integration |

## Dependency Rules

- Prefer `solomon-utils-core` for low-level helpers.
- Use `solomon-utils-spring` only when Spring container integration is required.
- Add `solomon-utils-json` explicitly when Jackson configuration or annotations are used.
- Keep `solomon-utils-algorithm` optional in runtime infrastructure.
