package cv.render.latex

import cv.model.EducationEntry
import cv.model.EducationSection
import cv.render.ElementRenderer

/** Renders the education timeline. */
internal object LatexEducationSectionRenderer : ElementRenderer<EducationSection, LatexRenderContext> {
    override fun render(element: EducationSection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            appendLine("""\begin{education}""")
            entries.forEachIndexed { index, entry ->
                append(LatexRendererBundle.educationEntryRenderer.render(entry, context.entry(index)))
            }
            appendLine("""\end{education}""")
        }
    }
}

internal object LatexEducationEntryRenderer : ElementRenderer<EducationEntry, LatexRenderContext> {
    override fun render(element: EducationEntry, context: LatexRenderContext): String = with(element) {
        """    \educationentry[${context.markKey}]{${latexEscape(years)}}{${LatexText.render(description)}}""" + "\n"
    }
}
