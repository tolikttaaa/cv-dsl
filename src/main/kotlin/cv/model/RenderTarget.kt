package cv.model

/**
 * Consumer-facing render targets of `cv-dsl` — the outputs a CV element can
 * appear in and the formats the Gradle `generateCv` task can produce.
 *
 * [PDF] covers the whole print pipeline: the generated LaTeX sources and the
 * compiled document. [WEB] is the static portfolio (whose page also embeds the
 * PDF, so producing the web deliverable implies producing the PDF).
 */
enum class RenderTarget {
    /** Static HTML portfolio. */
    WEB,

    /** LaTeX sources and the PDF compiled from them. */
    PDF,

    /** Single-file Markdown document. */
    MARKDOWN,
    ;

    companion object {
        /** Every render target — the default visibility of each CV element. */
        val all: Set<RenderTarget> = entries.toSet()
    }
}

/**
 * Where one CV element is rendered.
 *
 * An element appears in a target when the target is listed in [renderers] and
 * not listed in [excludedRenderers]. The default scope shows the element
 * everywhere; use [only] or [except] for the common one-sided cases.
 *
 * @property renderers Targets the element may appear in; defaults to all.
 * @property excludedRenderers Targets the element is hidden from; defaults to none.
 */
data class RenderScope(
    val renderers: Set<RenderTarget> = RenderTarget.all,
    val excludedRenderers: Set<RenderTarget> = emptySet(),
) {
    /** Whether the element is visible to [target]. */
    fun includes(target: RenderTarget): Boolean =
        target in renderers && target !in excludedRenderers

    companion object {
        /** The default scope: visible to every render target. */
        val all: RenderScope = RenderScope()

        /** Visible only to [targets]. */
        fun only(vararg targets: RenderTarget): RenderScope = RenderScope(renderers = targets.toSet())

        /** Visible everywhere except [targets]. */
        fun except(vararg targets: RenderTarget): RenderScope = RenderScope(excludedRenderers = targets.toSet())
    }
}
