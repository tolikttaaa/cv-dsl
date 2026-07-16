# Simple consumer

This directory is an executable consumer project, not a privileged internal
fixture. Its composite-build settings make local development use the repository
root in place of the released dependency.

From the repository root:

```bash
./gradlew -p examples/simple clean generateWeb
./gradlew -p examples/simple generateLatex
```

The portfolio appears in `examples/simple/build/web/index.html`. Run
`jwebserver -d examples/simple/build/web -p 8080` and open
`http://localhost:8080` to inspect it. PDF generation additionally requires
LuaLaTeX:

```bash
./gradlew -p examples/simple generatePdf
```

Copy this project as a starting point, then replace the composite build and
`io.github.tolikttaaa` development coordinate with the released JitPack setup
shown in the root README.
