package cv.render.latex

import cv.layout.LayoutKeys

/**
 * Per-element LaTeX state: the page-mark key of the element being rendered.
 *
 * Sections are rendered with their id as [markKey] and derive the keys of
 * their entries through [entry]. The keys match the [cv.layout.LayoutManifest]
 * written next to the sources, so the PDF build can map the pages LuaLaTeX
 * recorded back to model elements.
 */
internal data class LatexRenderContext(val markKey: String) {
    /** Context of the entry at [index] (0-based) inside this element. */
    fun entry(index: Int) = LatexRenderContext(LayoutKeys.entry(markKey, index))

    companion object {
        /** Context of header content, which carries no page marks. */
        val header = LatexRenderContext("header")
    }
}
