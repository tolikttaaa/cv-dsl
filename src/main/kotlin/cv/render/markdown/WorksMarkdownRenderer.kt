package cv.render.markdown

import cv.model.Work
import cv.model.WorksSection
import cv.render.ElementRenderer

/** Renders employment and teaching sections as Markdown. */
internal object MarkdownWorksSectionRenderer : ElementRenderer<WorksSection, MarkdownRenderContext> {
    override fun render(element: WorksSection, context: MarkdownRenderContext): String = with(element) {
        val entries = works.joinToString(separator = "\n\n") {
            MarkdownRendererBundle.workRenderer.render(it, context)
        }
        "## ${MarkdownText.escape(webTitle)}\n\n$entries"
    }
}

/** Renders one employment or teaching position as Markdown. */
internal object MarkdownWorkRenderer : ElementRenderer<Work, MarkdownRenderContext> {
    override fun render(element: Work, context: MarkdownRenderContext): String = with(element) {
        buildList {
            add("### ${MarkdownText.escape(role)}")
            add(
                listOf(
                    company.renderMarkdown(emphasized = true),
                    MarkdownText.escape(location),
                    MarkdownText.escape(dates),
                ).joinToString(separator = " · "),
            )
            add("")
            add(MarkdownText.markdown(description))
            if (tags.isNotEmpty()) {
                add("")
                add(tags.renderMarkdownTags())
            }
        }.joinToString(separator = "\n")
    }
}
