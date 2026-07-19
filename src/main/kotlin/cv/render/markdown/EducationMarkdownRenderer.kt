package cv.render.markdown

import cv.model.EducationEntry
import cv.model.EducationSection
import cv.render.ElementRenderer

/** Renders the education section as a Markdown list. */
internal object MarkdownEducationSectionRenderer : ElementRenderer<EducationSection, MarkdownRenderContext> {
    override fun render(element: EducationSection, context: MarkdownRenderContext): String = with(element) {
        val renderedEntries = entries.joinToString(separator = "\n") {
            MarkdownRendererBundle.educationEntryRenderer.render(it, context)
        }
        "## ${MarkdownText.escape(webTitle)}\n\n$renderedEntries"
    }
}

/** Renders one education milestone as a Markdown list item. */
internal object MarkdownEducationEntryRenderer : ElementRenderer<EducationEntry, MarkdownRenderContext> {
    override fun render(element: EducationEntry, context: MarkdownRenderContext): String = with(element) {
        "- **${MarkdownText.escape(years)}** — ${MarkdownText.markdown(description)}"
    }
}
