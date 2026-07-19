package cv.render.markdown

import cv.model.Referee
import cv.model.ReferencesSection
import cv.render.ElementRenderer

/** Renders the references section as Markdown. */
internal object MarkdownReferencesSectionRenderer : ElementRenderer<ReferencesSection, MarkdownRenderContext> {
    override fun render(element: ReferencesSection, context: MarkdownRenderContext): String = with(element) {
        val entries = referees.joinToString(separator = "\n\n") {
            MarkdownRendererBundle.refereeRenderer.render(it, context)
        }
        "## ${MarkdownText.escape(webTitle)}\n\n$entries"
    }
}

/** Renders one professional reference as Markdown. */
internal object MarkdownRefereeRenderer : ElementRenderer<Referee, MarkdownRenderContext> {
    override fun render(element: Referee, context: MarkdownRenderContext): String = with(element) {
        val details = listOf(
            MarkdownText.escape(role),
            company.renderMarkdown(emphasized = true),
            MarkdownText.escape(period),
        ).joinToString(separator = " · ")
        val emailLink = "[${MarkdownText.escape(email)}](${MarkdownText.escapeUrl("mailto:$email")})"
        "### ${MarkdownText.escape(name)}\n$details\n$emailLink"
    }
}
