package cv.render.web

import cv.dsl.DescriptionBuilder
import cv.dsl.richText
import cv.model.CvColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlTextTest {
    @Test
    fun `renders every inline style and escapes unsafe content`() {
        val text = richText {
            +"<&>\" "
            bold("bold")
            italic(" italic")
            colored(CvColor.ACCENT, " color")
            link("https://example.com?a=1&b=\"2\"", " link")
            nowrap(" fixed")
        }

        val html = HtmlText.html(text)
        assertTrue(html.contains("&lt;&amp;&gt;&quot;"))
        assertTrue(html.contains("<strong>bold</strong>"))
        assertTrue(html.contains("<em> italic</em>"))
        assertTrue(html.contains("color:${CvColor.ACCENT.css}"))
        assertTrue(html.contains("a=1&amp;b=&quot;2&quot;"))
        assertFalse(html.contains("nowrap"))
        assertEquals("<&>\" bold italic color link fixed", HtmlText.plain(text))
    }

    @Test
    fun `renders paragraphs and bullets in source order`() {
        val description = DescriptionBuilder().apply {
            paragraph("First   paragraph")
            bullets {
                item("one")
                item("two") { bold("two") }
            }
            paragraph("Last")
        }.build()

        assertEquals(
            "First paragraph <ul><li>one</li><li><strong>two</strong></li></ul> Last",
            HtmlText.html(description),
        )
    }

    @Test
    fun `escapes values for content and attributes`() {
        assertEquals("&lt;x a=&quot;1&quot;&gt;&amp;&lt;/x&gt;", h("<x a=\"1\">&</x>"))
    }
}
