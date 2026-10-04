package cv.render.latex

import cv.model.Project
import cv.model.ProjectsSection
import cv.render.ElementRenderer

/** Renders the projects' collection and individual project entries. */
internal object LatexProjectsSectionRenderer : ElementRenderer<ProjectsSection, LatexRenderContext> {
    override fun render(element: ProjectsSection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            appendLine("""\begin{projects}""")
            projects.forEachIndexed { index, project ->
                append(LatexRendererBundle.projectRenderer.render(project, context.entry(index)))
            }
            appendLine("""\end{projects}""")
        }
    }
}

internal object LatexProjectRenderer : ElementRenderer<Project, LatexRenderContext> {
    override fun render(element: Project, context: LatexRenderContext): String = with(element) {
        buildString {
            appendLine("""    \project[${context.markKey}]""")
            appendLine("""        {${latexEscape(name)}}""")
            appendLine("""        {${company.renderLatex(emphasized = true)}}""")
            appendLine("""        {${latexEscape(dates)}}""")
            appendLine("""        {""")
            append(description.renderLatexBlocks(indent = 3))
            appendLine("""        }""")
            appendLine("""        {${latexEscape(tags.joinToString(", "))}}""")
        }
    }
}
