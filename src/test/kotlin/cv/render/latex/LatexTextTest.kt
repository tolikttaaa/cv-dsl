package cv.render.latex

import cv.dsl.DescriptionBuilder
import cv.dsl.richText
import cv.model.CvColor
import cv.model.Organization
import cv.model.Social
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LatexTextTest {
    @Test
    fun `escapes every LaTeX special character`() {
        assertEquals(
            "\\textbackslash{}\\{\\}\\&\\%\\#\\_\\\$\\textasciitilde{}\\textasciicircum{}",
            LatexText.escape("\\{}&%#_\$~^"),
        )
        assertEquals("https://example.com/a\\%20b\\#part", LatexText.escapeUrl("https://example.com/a%20b#part"))
    }

    @Test
    fun `renders every inline style`() {
        val text = richText {
            +"plain "
            bold("bold")
            italic(" italic")
            colored(CvColor.BASE, " color")
            link("https://example.com/#part", " link")
            nowrap(" fixed")
        }

        val latex = LatexText.render(text)
        assertTrue(latex.contains("\\textbf{bold}"))
        assertTrue(latex.contains("\\emph{ italic}"))
        assertTrue(latex.contains("\\textcolor{basecolor}{ color}"))
        assertTrue(latex.contains("\\link{https://example.com/\\#part}{ link}"))
        assertTrue(latex.contains("\\mbox{ fixed}"))
    }

    @Test
    fun `renders paragraphs and bullet lists`() {
        val description = DescriptionBuilder().apply {
            paragraph("Intro & context")
            bullets {
                item("first")
                item("second") { bold("second") }
            }
        }.build()

        val latex = description.renderLatexBlocks(indent = 1)
        assertTrue(latex.contains("    Intro \\& context"))
        assertTrue(latex.contains("\\begin{itemize}"))
        assertTrue(latex.contains("\\item {\\textbf{second}}"))
    }

    @Test
    fun `renders linked and unlinked organizations`() {
        assertEquals("\\textbf{Independent}", Organization("Independent").renderLatex(emphasized = true))
        assertEquals(
            "\\link{https://example.com/a\\#b}{Example}",
            Organization("Example", "https://example.com/a#b").renderLatex(emphasized = false),
        )
    }

    @Test
    fun `renders every social contact command`() {
        val rendered = listOf(
            Social.Phone("+1") to "\\smartphone{+1}",
            Social.Telegram("ada") to "\\telegram{ada}",
            Social.Email("a_b@example.com") to "\\email{a\\_b@example.com}",
            Social.LinkedIn("ada") to "\\linkedin{ada}",
            Social.LeetCode("ada") to "\\leetcode{ada}",
            Social.GitHub("ada") to "\\github{ada}",
            Social.Address("London & UK") to "\\address{London \\& UK}",
        )

        rendered.forEach { (social, expected) ->
            assertEquals(expected, LatexSocialRenderer.render(social, Unit))
        }
    }
}
