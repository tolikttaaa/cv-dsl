package cv.render.latex

import cv.dsl.cv
import cv.layout.LayoutManifest
import cv.model.Organization
import cv.model.PageFit
import cv.model.RenderScope
import cv.model.RenderTarget
import cv.testing.sampleCv
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LatexLayoutRenderingTest {
    @TempDir
    lateinit var output: Path

    @Test
    fun `sets the base font size only when configured`() {
        assertFalse(render(sampleCv).resolve("cv.tex").readText().contains("\\cvfontsize"))

        val document = render(cv { pdf { fontSize = 9.5 }; summary("S", "faUser") { paragraph("t") } }, "half")
            .resolve("cv.tex").readText()
        assertTrue(document.startsWith("\\documentclass[localFont,alternative]{cvdsl}\n\\cvfontsize{9.5pt}\n"))
        val whole = render(cv { pdf { fontSize = 9.0 }; summary("S", "faUser") { paragraph("t") } }, "whole")
        assertTrue(whole.resolve("cv.tex").readText().contains("\\cvfontsize{9pt}\n"))
    }

    @Test
    fun `marks every section and entry with its layout key`() {
        val latex = render(sampleCv)

        val summary = latex.resolve("sections/summary.tex").readText()
        assertTrue(summary.contains("\\sectionTitle[summary]{Summary}{\\faUser}"))
        assertTrue(summary.contains("\\summaryText[summary]{"))
        assertTrue(latex.resolve("sections/experience.tex").readText().contains("\\work[experience/1]\n"))
        assertTrue(latex.resolve("sections/skills.tex").readText().contains("\\keywordsentry[skills/1]{Technical}"))
        assertTrue(latex.resolve("sections/projects.tex").readText().contains("\\project[projects/1]\n"))
        val education = latex.resolve("sections/education.tex").readText()
        assertTrue(education.contains("\\educationentry[education/1]{1828 – 1835}"))
        assertTrue(education.contains("\\educationentry[education/2]{1835}"))
        assertTrue(latex.resolve("sections/references.tex").readText().contains("\\referee[references/1]\n"))
        assertEquals(LayoutManifest.of(sampleCv), LayoutManifest.parse(latex.resolve("layout.tsv").readText()))
    }

    @Test
    fun `numbers entries after removing those hidden from the PDF`() {
        val scoped = cv {
            pdf { maxPages = 2 }
            experience("Experience", "faSuitcase", id = "experience", pageFit = PageFit.OnPage(1)) {
                work("Web only", Organization("A"), "City", "2020", listOf("Tag"), RenderScope.only(RenderTarget.WEB)) {
                    paragraph("hidden")
                }
                work("Printed", Organization("B"), "City", "2021", listOf("Tag"), pageFit = PageFit.SinglePage) {
                    paragraph("shown")
                }
            }
        }
        val latex = render(scoped)

        val experience = latex.resolve("sections/experience.tex").readText()
        assertTrue(experience.contains("\\work[experience/1]\n        {Printed}"))
        assertFalse(experience.contains("Web only"))
        assertEquals(
            listOf("experience" to PageFit.OnPage(1), "experience/1" to PageFit.SinglePage),
            LayoutManifest.parse(latex.resolve("layout.tsv").readText()).elements.map { it.key to it.pageFit },
        )
    }

    @Test
    fun `rejects page rules beyond the page limit before writing sources`() {
        val impossible = cv {
            pdf { maxPages = 1 }
            summary("Summary", "faUser", pageFit = PageFit.OnPage(2)) { paragraph("text") }
        }
        val error = assertFailsWith<IllegalArgumentException> { render(impossible, "impossible") }
        assertEquals("Summary must be on page 2, but the PDF is limited to 1 page", error.message)
        assertFalse(output.resolve("impossible").toFile().exists())
    }

    private fun render(document: cv.model.Cv, name: String = "latex"): Path =
        output.resolve(name).also { LatexRenderer.render(document, it) }
}
