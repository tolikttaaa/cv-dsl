# Contributing

## Before changing code

1. Open an issue for behavior changes that affect the public DSL, generated
   HTML/LaTeX, plugin task names, or output layout.
2. Branch from `main` and use conventional commit subjects (`feat:`, `fix:`,
   `docs:`, `test:`, `refactor:`, `build:`). Release Please uses these to build
   the changelog and choose the next semantic version.
3. Keep the model renderer-independent. HTML and LaTeX decisions belong in
   their renderer packages, not in `cv.model` or `cv.dsl`.

## Required implementation sequence

For a new model element:

1. add the immutable type under `cv.model`;
2. expose it through the smallest appropriate builder under `cv.dsl`;
3. add a renderer contract when it is a new leaf type;
4. implement and register both web and LaTeX renderers;
5. extend the simple example so the feature can be inspected manually;
6. add focused model/DSL tests and assertions against both generated formats;
7. update README/API documentation and architecture notes.

For a new section type, update the sealed `Section` hierarchy and exhaustive
`Section.renderWith` dispatch. The build must never leave one output format
silently unsupported.

For a Gradle plugin change, test extension defaults, task registration and at
least one observable task behavior. Keep task inputs and outputs annotated so
Gradle caching and up-to-date checks remain correct.

## Test and coverage policy

Every behavior change needs a test that fails without the change. Choose the
lowest useful level:

- unit tests for builders, normalization, validation and escaping;
- renderer tests for exact fragments and edge cases;
- integration tests for generated directory/file contracts;
- Gradle plugin/task tests for build integration;
- the `examples/simple` smoke build for the consumer experience.

Run before opening a pull request:

```bash
./gradlew clean check
./gradlew -p examples/simple clean generateWeb
```

CI enforces at least 70% line coverage across the repository. New or changed
production code should normally be at least 90% covered; do not satisfy the
global gate by testing unrelated lines. Inspect the HTML report at
`build/reports/jacoco/test/html/index.html` and mention intentionally uncovered
error paths in the pull request.

Generated output is test evidence, not source: do not commit `build/` files.
When markup changes intentionally, assert semantic anchors rather than copying
an entire brittle document snapshot.

## Pull request checklist

- [ ] Public behavior and compatibility impact are described.
- [ ] Both web and LaTeX paths are implemented where applicable.
- [ ] Tests reproduce the change and coverage remains above the gate.
- [ ] The simple example still generates and demonstrates new public behavior.
- [ ] README, KDoc, architecture and release notes are updated as needed.
- [ ] `./gradlew clean check` and the example smoke build pass locally.
