package cv.render.markdown

import cv.model.SummarySection
import cv.render.ElementRenderer

/** Renders the summary section as Markdown. */
internal object MarkdownSummarySectionRenderer : ElementRenderer<SummarySection, MarkdownRenderContext> {
    override fun render(element: SummarySection, context: MarkdownRenderContext): String = with(element) {
        "## ${MarkdownText.escape(webTitle)}\n\n${MarkdownText.markdown(text)}"
    }
}
