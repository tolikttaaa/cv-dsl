package cv.render.latex

import cv.model.Cv
import java.math.BigDecimal

/** Renders the root LaTeX document around the independently rendered section files. */
internal fun Cv.renderLatexDocument(): String = buildString {
    appendLine("""\documentclass[localFont,alternative]{cvdsl}""")
    pdf.fontSize?.let { appendLine("""\cvfontsize{${it.toLatexPoints()}pt}""") }
    if (!hyphenation) {
        // Forbid hyphenation document-wide; emergencystretch lets justified
        // lines breathe instead of overflowing once words must stay whole.
        appendLine("""\hyphenpenalty=10000""")
        appendLine("""\exhyphenpenalty=10000""")
        appendLine("""\emergencystretch=3em""")
    }
    appendLine("""\name{${latexEscape(firstName.uppercase())}}{${latexEscape(lastName.uppercase())}}""")
    appendLine("""\tagline{${latexEscape(tagline)}}""")
    appendLine()
    appendLine("""\socialinfo{""")
    append(social.joinToString(" \\\\\n") { row ->
        "    " + row.joinToString(" ") { LatexRendererBundle.socialRenderer.render(it, LatexRenderContext.header) }
    })
    appendLine()
    appendLine("""}""")
    appendLine()
    photo?.let {
        appendLine("""\photo{${it.size}}{${it.file}}""")
        appendLine()
    }
    appendLine("""\begin{document}""")
    appendLine("""    \makecvheader""")
    appendLine("""    \makecvfooter{}{\textsc{${latexEscape(footerText)}}}{\thepage}""")
    appendLine()
    for (section in sections) {
        appendLine("""    \input{sections/${section.id}}""")
    }
    appendLine("""\end{document}""")
}

/** Formats a point size without a redundant fraction: `9.0` → `9`, `9.5` → `9.5`. */
private fun Double.toLatexPoints(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
