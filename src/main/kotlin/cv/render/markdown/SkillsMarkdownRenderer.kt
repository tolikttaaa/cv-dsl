package cv.render.markdown

import cv.model.SkillEntry
import cv.model.SkillsSection
import cv.render.ElementRenderer

/** Renders the skills section as a Markdown table. */
internal object MarkdownSkillsSectionRenderer : ElementRenderer<SkillsSection, MarkdownRenderContext> {
    override fun render(element: SkillsSection, context: MarkdownRenderContext): String = with(element) {
        val rows = entries.joinToString(separator = "\n") {
            MarkdownRendererBundle.skillEntryRenderer.render(it, context)
        }
        listOf(
            "## ${MarkdownText.escape(webTitle)}",
            "",
            "| Category | Skills |",
            "| --- | --- |",
            rows,
        ).joinToString(separator = "\n").trimEnd()
    }
}

/** Renders one skills table row as Markdown. */
internal object MarkdownSkillEntryRenderer : ElementRenderer<SkillEntry, MarkdownRenderContext> {
    override fun render(element: SkillEntry, context: MarkdownRenderContext): String = with(element) {
        val renderedCategory = MarkdownText.escape(category)
        val renderedSkills = MarkdownText.escape(skills.joinToString(separator = ", "))
        "| $renderedCategory | $renderedSkills |"
    }
}
