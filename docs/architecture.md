# Architecture

`cv-dsl` follows a one-way pipeline:

```text
Kotlin DSL → immutable model → renderer bundle → generated files
                                      ├── web
                                      ├── LaTeX
                                      └── Markdown
```

## Packages and ownership

| Package | Responsibility | Must not own |
|---|---|---|
| `cv.model` | Immutable document tree | HTML, LaTeX, filesystem behavior |
| `cv.dsl` | Type-safe builders and rich-text matching | Output formatting |
| `cv.render` | Format contracts, dispatch and validation | Consumer content |
| `cv.render.web` | Safe HTML and bundled browser assets | LaTeX behavior |
| `cv.render.latex` | Escaped LaTeX and bundled class/fonts | Browser behavior |
| `cv.render.markdown` | Escaped single-file Markdown document | HTML and LaTeX behavior |
| `cv.layout` | PDF layout manifest, page marks and rule verification | LaTeX markup, Gradle lifecycle |
| `cv.generation` | Output selection and consumer asset copying | Gradle lifecycle |
| `cv.gradle` | Consumer tasks and local tool orchestration | CV content |

`Section` is sealed. `RendererBundle<C>` is the composition root for a format,
and `Section.renderWith` exhaustively dispatches every section type. Adding a
section therefore produces compiler errors until every renderer bundle supports
it.

Element visibility is data, not renderer behavior: every section, entry and
contact carries a `RenderScope` (allow-list plus deny-list of `RenderTarget`s,
defaulting to "everywhere"). Each `CvRenderer` prunes the model once through
`Cv.visibleTo(target)` before rendering, so section renderers never need to
consult scopes.

## PDF layout verification

Page rules (`Cv.pdf.maxPages`, `PageFit` on sections and entries) are checked
against the compiled PDF in three steps:

1. `LatexRenderer` writes `layout.tsv` next to the sources: every tracked
   element with its key, label and rule. Section keys are their ids; entry
   keys extend them (`experience/2`), so an element's pages include its
   entries' pages. `LatexRenderContext` hands each renderer its key.
2. `cvdsl.cls` records `mark <key> <page>` lines in `cv.pagemarks` while the
   PDF is shipped out (`\cvpagemark`, via the optional first argument of
   `\sectionTitle` and every entry command), followed by the page count.
3. `CompileCvPdfTask` combines both into a `PdfLayoutReport`, writes
   `cv-layout.txt` and fails on any violated rule.

Marks sit inside an element's boxes, or right after a section title followed
by `\nobreak`, so they never add a page breakpoint or move a baseline.

## Public compatibility

The public API includes DSL methods, model constructors/properties,
`CvApplication`, renderer contracts, the `cv.dsl.generation` plugin id, its
extension properties, task names and documented output paths. Changes to these
surfaces require explicit compatibility notes and an appropriate semantic
version bump.

Generated HTML structure and LaTeX commands are observable behavior. Tests
should protect important classes, links, escaping, file names and section
ordering while avoiding full-document snapshots that obscure intentional
changes.

## Safety boundaries

All content text is escaped by the target renderer. Section IDs, icon command
names, photo paths and photo sizes are validated before files are written to
prevent directory traversal and generated-markup injection. Consumer assets
are copied only beneath their selected output directory.

External tools are intentionally kept at the edge: only PDF compilation needs
LuaLaTeX, and only local preview needs the JDK's `jwebserver`. Unit and web
generation tests do not depend on either process.
