package cv.render.markdown

import cv.model.Cv
import cv.render.renderWith

/** Renders the shared document chrome around the Markdown CV sections. */
internal fun Cv.renderMarkdownDocument(): String {
    val context = MarkdownRenderContext(this)
    val fullName = MarkdownText.escape("$firstName $lastName")
    val socialRows = social.joinToString(separator = "\n") { row ->
        row.joinToString(separator = " · ") {
            MarkdownRendererBundle.socialRenderer.render(it, context)
        }
    }
    val renderedSections = sections.map { it.renderWith(MarkdownRendererBundle, context) }
    val documentParts = buildList {
        add("# $fullName\n*${MarkdownText.escape(tagline)}*")
        if (social.isNotEmpty()) add(socialRows)
        addAll(renderedSections)
    }
    return documentParts.joinToString(separator = "\n\n").trimEnd() + "\n"
}
