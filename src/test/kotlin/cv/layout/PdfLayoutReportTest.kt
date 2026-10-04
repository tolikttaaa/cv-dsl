package cv.layout

import cv.model.PageFit
import cv.testing.assertReportRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PdfLayoutReportTest {
    private val marks = PageMarks.parse(
        """
        mark summary 1
        mark experience 1
        mark experience/1 1
        mark experience/1 1
        mark experience/2 1
        mark experience/2 2
        mark experience/10 3
        mark skills 2
        pages 3
        """.trimIndent(),
    )

    @Test
    fun `spans cover an element and everything nested in it`() {
        assertEquals(3, marks.pageCount)
        assertEquals(1..1, marks.span("summary"))
        assertEquals(1..2, marks.span("experience/2"))
        assertEquals(3..3, marks.span("experience/10"))
        assertEquals(1..3, marks.span("experience"))
        assertNull(marks.span("experience/3"))
        assertNull(marks.span("exp"))
        assertNull(PageMarks.parse("mark a 1").pageCount)
    }

    @Test
    fun `rejects malformed page marks`() {
        listOf("mark a", "mark a one", "pages", "pages x", "note a 1").forEach { line ->
            val error = assertFailsWith<IllegalArgumentException> { PageMarks.parse(line) }
            assertTrue(error.message.orEmpty().contains(line))
        }
    }

    @Test
    fun `reports every violated rule and where each element landed`() {
        val manifest = LayoutManifest(
            maxPages = 2,
            elements = listOf(
                LayoutElement("summary", "Summary", PageFit.OnPage(1)),
                LayoutElement("experience", "Experience", PageFit.OnPage(1)),
                LayoutElement("experience/1", "Experience › Azul", PageFit.SinglePage),
                LayoutElement("experience/2", "Experience › Yandex", PageFit.SinglePage),
                LayoutElement("skills", "Skills", PageFit.Anywhere),
                LayoutElement("missing", "Missing", PageFit.OnPage(2)),
                LayoutElement("lost", "Lost", PageFit.SinglePage),
            ),
        )

        val report = PdfLayoutReport.of(manifest, marks)

        assertEquals(
            listOf(
                "The PDF has 3 pages, but is limited to 2",
                "Experience must fit on page 1, but occupies pages 1–3",
                "Experience › Yandex must fit on a single page, but spans pages 1–2",
                "Missing must be on page 2, but it is missing from the PDF",
                "Lost must fit on a single page, but it is missing from the PDF",
            ),
            report.violations,
        )
        assertEquals("PDF layout: 3 pages, 5 of 7 page rules violated", report.summary)
        val rendered = report.render()
        assertTrue(rendered.startsWith("PDF layout: 3 pages, 5 of 7 page rules violated\n  ✗ The PDF has 3 pages"))
        assertReportRow(rendered, "page 1", "on page 1", "Summary")
        assertReportRow(rendered, "pages 1–3", "on page 1", "Experience", violated = true)
        assertReportRow(rendered, "pages 1–2", "single page", "Experience › Yandex", violated = true)
        assertReportRow(rendered, "page 2", "-", "Skills")
        assertReportRow(rendered, "missing", "on page 2", "Missing", violated = true)
    }

    @Test
    fun `summarizes satisfied documents and documents without rules`() {
        val satisfied = PdfLayoutReport.of(
            LayoutManifest(3, listOf(LayoutElement("summary", "Summary", PageFit.OnPage(1)))),
            marks,
        )
        assertEquals(emptyList(), satisfied.violations)
        assertEquals("PDF layout: 3 pages, all 2 page rules hold", satisfied.summary)

        val unconstrained = PdfLayoutReport.of(LayoutManifest(null, emptyList()), PageMarks.parse("pages 1"))
        assertEquals("PDF layout: 1 page, no page rules declared", unconstrained.summary)

        val unknown = PdfLayoutReport.of(LayoutManifest(1, emptyList()), PageMarks.parse(""))
        assertEquals(listOf("The PDF is limited to 1 page, but its page count was not recorded"), unknown.violations)
        assertEquals("PDF layout: unknown page count, 1 of 1 page rules violated", unknown.summary)
    }
}
