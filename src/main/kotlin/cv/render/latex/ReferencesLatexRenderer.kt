package cv.render.latex

import cv.model.Referee
import cv.model.ReferencesSection
import cv.render.ElementRenderer

/** Renders the references collection and individual referee entries. */
internal object LatexReferencesSectionRenderer : ElementRenderer<ReferencesSection, LatexRenderContext> {
    override fun render(element: ReferencesSection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            appendLine("""\begin{referees}""")
            referees.forEachIndexed { index, referee ->
                append(LatexRendererBundle.refereeRenderer.render(referee, context.entry(index)))
            }
            appendLine("""\end{referees}""")
        }
    }
}

internal object LatexRefereeRenderer : ElementRenderer<Referee, LatexRenderContext> {
    override fun render(element: Referee, context: LatexRenderContext): String = with(element) {
        buildString {
            appendLine("""    \referee[${context.markKey}]""")
            appendLine("""        {${latexEscape(name)}}""")
            appendLine("""        {${latexEscape(role)}}""")
            appendLine("""        {${company.renderLatex(emphasized = false)}} {${latexEscape(period)}}""")
            appendLine("""        {${latexEscape(email)}}""")
        }
    }
}
