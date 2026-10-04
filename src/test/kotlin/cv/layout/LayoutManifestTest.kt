package cv.layout

import cv.dsl.cv
import cv.model.Organization
import cv.model.PageFit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LayoutManifestTest {
    private val document = cv {
        pdf { maxPages = 2 }
        summary("Summary", "faUser") { paragraph("text") }
        experience("Experience", "faSuitcase", id = "experience", pageFit = PageFit.OnPage(1)) {
            work("Engineer", Organization("Azul"), "City", "2025", emptyList()) { paragraph("text") }
            work("Developer", Organization("Yandex"), "City", "2023", emptyList(), pageFit = PageFit.OnPage(1)) {
                paragraph("text")
            }
        }
        skills("Skills", "faCode") { entry("Languages", listOf("Kotlin"), pageFit = PageFit.SinglePage) }
        projects("Projects", "faLaptop") {
            project("Tool", Organization("Org"), "2022", emptyList()) { paragraph("text") }
        }
        education("Education", "faUserGraduate") { entry("2018") { +"School" } }
        references("References", "faQuoteLeft") {
            referee("Ada", "Role", Organization("Org"), "2020", "ada@example.com")
        }
    }

    @Test
    fun `lists every section and entry with entry keys extending the section id`() {
        val manifest = LayoutManifest.of(document)

        assertEquals(2, manifest.maxPages)
        assertEquals(
            listOf(
                LayoutElement("summary", "Summary", PageFit.Anywhere),
                LayoutElement("experience", "Experience", PageFit.OnPage(1)),
                LayoutElement("experience/1", "Experience › Engineer, Azul", PageFit.Anywhere),
                LayoutElement("experience/2", "Experience › Developer, Yandex", PageFit.OnPage(1)),
                LayoutElement("skills", "Skills", PageFit.Anywhere),
                LayoutElement("skills/1", "Skills › Languages", PageFit.SinglePage),
                LayoutElement("projects", "Projects", PageFit.Anywhere),
                LayoutElement("projects/1", "Projects › Tool", PageFit.Anywhere),
                LayoutElement("education", "Education", PageFit.Anywhere),
                LayoutElement("education/1", "Education › 2018", PageFit.Anywhere),
                LayoutElement("references", "References", PageFit.Anywhere),
                LayoutElement("references/1", "References › Ada", PageFit.Anywhere),
            ),
            manifest.elements,
        )
        assertTrue(manifest.hasRules)
        assertFalse(LayoutManifest(null, listOf(LayoutElement("a", "A", PageFit.Anywhere))).hasRules)
    }

    @Test
    fun `round-trips through its text form and flattens labels to one line`() {
        val manifest = LayoutManifest.of(document)
        assertEquals(manifest, LayoutManifest.parse(manifest.render()))

        val multiline = LayoutManifest(null, listOf(LayoutElement("a", "Two\tword\nlabel", PageFit.SinglePage)))
        assertEquals("Two word label", LayoutManifest.parse(multiline.render()).elements.single().label)
    }

    @Test
    fun `rejects malformed manifest lines`() {
        listOf("max-pages\tmany", "element\ta\tsomewhere\tA", "element\ta\tpage:0\tA", "element\ta", "unknown")
            .forEach { line ->
                val error = assertFailsWith<IllegalArgumentException> { LayoutManifest.parse(line) }
                assertTrue(error.message.orEmpty().contains(line))
            }
    }

    @Test
    fun `rejects rules that no layout can satisfy`() {
        val beyondLimit = LayoutManifest(1, listOf(LayoutElement("experience", "Experience", PageFit.OnPage(2))))
        val limit = assertFailsWith<IllegalArgumentException> { beyondLimit.validate() }
        assertEquals("Experience must be on page 2, but the PDF is limited to 1 page", limit.message)

        val conflicting = LayoutManifest(
            null,
            listOf(
                LayoutElement("experience", "Experience", PageFit.OnPage(1)),
                LayoutElement("experience/1", "Experience › Role", PageFit.OnPage(2)),
            ),
        )
        val conflict = assertFailsWith<IllegalArgumentException> { conflicting.validate() }
        assertEquals("Experience › Role must be on page 2, but its section must be on page 1", conflict.message)

        LayoutManifest.of(document).validate()
    }
}
