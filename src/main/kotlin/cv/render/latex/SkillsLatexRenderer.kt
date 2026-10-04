package cv.render.latex

import cv.model.SkillEntry
import cv.model.SkillsSection
import cv.render.ElementRenderer

/** Renders the skills keyword table. */
internal object LatexSkillsSectionRenderer : ElementRenderer<SkillsSection, LatexRenderContext> {
    override fun render(element: SkillsSection, context: LatexRenderContext): String = with(element) {
        buildString {
            append(renderLatexTitle(context))
            appendLine("""\begin{keywords}""")
            entries.forEachIndexed { index, entry ->
                append(LatexRendererBundle.skillEntryRenderer.render(entry, context.entry(index)))
            }
            appendLine("""\end{keywords}""")
        }
    }
}

internal object LatexSkillEntryRenderer : ElementRenderer<SkillEntry, LatexRenderContext> {
    override fun render(element: SkillEntry, context: LatexRenderContext): String = with(element) {
        val values = latexEscape(skills.joinToString(", "))
        """    \keywordsentry[${context.markKey}]{${latexEscape(category)}}{$values}""" + "\n"
    }
}
