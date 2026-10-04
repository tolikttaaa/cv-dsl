package cv.render.latex

import cv.model.SummarySection
import cv.render.ElementRenderer

/** Renders the free-form summary section. */
internal object LatexSummarySectionRenderer : ElementRenderer<SummarySection, LatexRenderContext> {
    override fun render(element: SummarySection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            appendLine("""\begin{summary}""")
            appendLine("""    \summaryText[${context.markKey}]{""")
            append(text.renderLatexBlocks(indent = 2))
            appendLine("""    }""")
            appendLine("""\end{summary}""")
        }
    }
}
