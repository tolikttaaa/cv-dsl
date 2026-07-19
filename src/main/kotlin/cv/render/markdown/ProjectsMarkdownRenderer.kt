package cv.render.markdown

import cv.model.Project
import cv.model.ProjectsSection
import cv.render.ElementRenderer

/** Renders the projects section as Markdown. */
internal object MarkdownProjectsSectionRenderer : ElementRenderer<ProjectsSection, MarkdownRenderContext> {
    override fun render(element: ProjectsSection, context: MarkdownRenderContext): String = with(element) {
        val entries = projects.joinToString(separator = "\n\n") {
            MarkdownRendererBundle.projectRenderer.render(it, context)
        }
        "## ${MarkdownText.escape(webTitle)}\n\n$entries"
    }
}

/** Renders one project as Markdown. */
internal object MarkdownProjectRenderer : ElementRenderer<Project, MarkdownRenderContext> {
    override fun render(element: Project, context: MarkdownRenderContext): String = with(element) {
        buildList {
            add("### ${MarkdownText.escape(name)}")
            add(
                listOf(
                    company.renderMarkdown(emphasized = true),
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
