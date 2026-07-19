package cv.render.markdown

import cv.dsl.DescriptionBuilder
import cv.dsl.richText
import cv.model.CvColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkdownTextTest {
    @Test
    fun `renders every inline style and escapes unsafe content`() {
        val text = richText {
            +"*_[]# "
            bold("bold")
            italic(" italic")
            colored(CvColor.ACCENT, " color")
            link("https://example.com/a b(1)", " link")
            nowrap(" fixed")
        }

        val markdown = MarkdownText.markdown(text)
        assertTrue(markdown.contains("\\*\\_\\[\\]\\#"))
        assertTrue(markdown.contains("**bold**"))
        assertTrue(markdown.contains("* italic*"))
        assertTrue(markdown.contains("[ link](https://example.com/a%20b\\(1\\))"))
        assertFalse(markdown.contains(CvColor.ACCENT.css))
        assertFalse(markdown.contains("nowrap"))
        assertEquals("*_[]# bold italic color link fixed", MarkdownText.plain(text))
    }

    @Test
    fun `renders paragraphs and bullets as separate blocks`() {
        val description = DescriptionBuilder().apply {
            paragraph("First   paragraph")
            bullets {
                item("one")
                item("two") { bold("two") }
            }
            paragraph("Last")
        }.build()

        assertEquals(
            "First paragraph\n\n- one\n- **two**\n\nLast",
            MarkdownText.markdown(description),
        )
    }

    @Test
    fun `escapes markdown syntax characters`() {
        assertEquals("\\`code\\` \\\\ \\<tag\\> \\|cell\\|", MarkdownText.escape("`code` \\ <tag> |cell|"))
        assertEquals("a&b", MarkdownText.escape("a&b"))
    }
}
