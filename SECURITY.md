# Security Policy

## Supported Versions

Security fixes are handled on the current development line and the latest published release when practical. Older release builds may not receive separate patches unless the issue is severe and the fix can be applied safely.

## Reporting a Vulnerability

Please do not open a public issue for a suspected vulnerability.

Report security concerns privately through GitHub's private vulnerability reporting if it is enabled for the repository, or contact the maintainers directly. Include:

- A clear description of the issue.
- Steps to reproduce.
- Affected app version or commit.
- Android version and device model if relevant.
- Any logs, screenshots, or proof-of-concept details that do not expose private data.

Do not include copyrighted music files, private libraries, secrets, signing keys, or personal data in the report.

## Scope

Security-sensitive areas include:

- Local file access and storage permissions.
- Metadata, lyric, artwork, and sidecar-file writes.
- Handling malformed audio files or tags.
- Network calls to lyric and artwork services.
- Release signing, build artifacts, and GitHub Actions secrets.
- Any behavior that could leak local library paths, filenames, tags, or user data.

## Expectations

The maintainers will acknowledge valid reports when possible, investigate with the reporter if more detail is needed, and publish fixes with appropriate release notes once a safe fix is ready.

Please give maintainers reasonable time to investigate before public disclosure.
