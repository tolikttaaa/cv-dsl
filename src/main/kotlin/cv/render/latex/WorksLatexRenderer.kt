package cv.render.latex

import cv.model.Bullets
import cv.model.Work
import cv.model.WorksSection
import cv.render.ElementRenderer

/** Renders employment and teaching sections, which share the works layout. */
internal object LatexWorksSectionRenderer : ElementRenderer<WorksSection, LatexRenderContext> {
    override fun render(element: WorksSection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            if (works.any { work -> work.description.blocks.any { it is Bullets } }) {
                appendLine("""\renewcommand{\labelitemi}{${'$'}\bullet${'$'}}""")
            }
            appendLine("""\begin{works}""")
            works.forEachIndexed { index, work ->
                append(LatexRendererBundle.workRenderer.render(work, context.entry(index)))
            }
            appendLine("""\end{works}""")
        }
    }
}

internal object LatexWorkRenderer : ElementRenderer<Work, LatexRenderContext> {
    override fun render(element: Work, context: LatexRenderContext): String = with(element) {
        buildString {
            val renderedCompany = company.renderLatex(emphasized = true)
            appendLine("""    \work[${context.markKey}]""")
            appendLine(
                """        {${latexEscape(role)}} {$renderedCompany} """ +
                    """{${latexEscape(location)}} {${latexEscape(dates)}}""",
            )
            appendLine("""        {""")
            append(description.renderLatexBlocks(indent = 3))
            appendLine("""        }""")
            appendLine("""        {${latexEscape(tags.joinToString(", "))}}""")
        }
    }
}
