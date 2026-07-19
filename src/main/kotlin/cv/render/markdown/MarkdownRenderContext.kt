package cv.render.markdown

import cv.model.Cv

/**
 * Shared immutable state available while producing one Markdown document.
 * Leaf renderers receive the same context for consistency with other formats.
 */
internal data class MarkdownRenderContext(val cv: Cv)
