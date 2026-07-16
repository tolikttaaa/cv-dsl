package cv.render

import cv.dsl.cv
import cv.render.latex.LatexRenderer
import cv.render.web.WebRenderer
import cv.testing.sampleCv
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RendererIntegrationTest {
    @TempDir
    lateinit var output: Path

    @Test
    fun `factory returns both supported renderer implementations`() {
        assertEquals(WebRenderer, CvRendererFactory.create(RenderFormat.Web))
        assertEquals(LatexRenderer, CvRendererFactory.create(RenderFormat.Latex))
    }

    @Test
    fun `renders a complete static portfolio with bundled assets`() {
        val web = output.resolve("web")
        WebRenderer.render(sampleCv, web)

        assertEquals(
            setOf("index.html", "styles.css", "app.js", "pdf-viewer.js", "favicon.png"),
            Files.list(web).use { files -> files.map { it.fileName.toString() }.toList().toSet() },
        )
        val html = web.resolve("index.html").readText()
        assertTrue(html.contains("<title>Ada Lovelace — Portfolio</title>"))
        assertTrue(html.contains("Computing pioneer &amp; mathematician"))
        assertTrue(html.contains("data-title=\"About\""))
        assertTrue(html.contains("Analytical Engines"))
        assertTrue(html.contains("Bernoulli numbers"))
        assertTrue(html.contains("charles@example.com"))
        assertTrue(html.contains("github.com/ada"))
    }

    @Test
    fun `renders complete LaTeX sources and bundled template`() {
        val latex = output.resolve("latex")
        LatexRenderer.render(sampleCv, latex)

        assertTrue(Files.isRegularFile(latex.resolve("cv.tex")))
        assertTrue(Files.isRegularFile(latex.resolve("cvdsl.cls")))
        assertEquals(13, Files.list(latex.resolve("fonts")).use { it.count() })
        assertEquals(
            sampleCv.sections.map { "${it.id}.tex" }.toSet(),
            Files.list(latex.resolve("sections")).use { files ->
                files.map { it.fileName.toString() }.toList().toSet()
            },
        )
        val document = latex.resolve("cv.tex").readText()
        assertTrue(document.contains("\\hyphenpenalty=10000"))
        assertTrue(document.contains("\\name{ADA}{LOVELACE}"))
        assertTrue(document.contains("\\photo{2.2cm}{portrait.png}"))
        assertTrue(document.contains("\\input{sections/references}"))
        assertTrue(latex.resolve("sections/experience.tex").readText().contains("\\begin{works}"))
    }

    @Test
    fun `web rendering requires at least one section`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            WebRenderer.render(cv {}, output.resolve("empty"))
        }
        assertTrue(exception.message.orEmpty().contains("at least one section"))
    }

    @Test
    fun `rejects duplicate and unsafe section ids`() {
        val duplicate = cv {
            summary("One", "faUser", id = "same") { paragraph("one") }
            summary("Two", "faUser", id = "same") { paragraph("two") }
        }
        assertFailsWith<IllegalArgumentException> { LatexRenderer.render(duplicate, output.resolve("duplicate")) }

        val traversal = cv {
            summary("Unsafe", "faUser", id = "../outside") { paragraph("no") }
        }
        assertFailsWith<IllegalArgumentException> { LatexRenderer.render(traversal, output.resolve("traversal")) }
    }

    @Test
    fun `rejects unsafe photo paths sizes and section icons`() {
        val unsafePhoto = cv {
            photo("../portrait.png")
            summary("Summary", "faUser") { paragraph("text") }
        }
        assertFailsWith<IllegalArgumentException> { WebRenderer.render(unsafePhoto, output.resolve("photo")) }

        val unsafeSize = cv {
            photo("portrait.png", "2cm}\\input{evil}")
            summary("Summary", "faUser") { paragraph("text") }
        }
        assertFailsWith<IllegalArgumentException> { LatexRenderer.render(unsafeSize, output.resolve("size")) }

        val unsafeIcon = cv {
            summary("Summary", "faUser}\\input{evil}") { paragraph("text") }
        }
        assertFailsWith<IllegalArgumentException> { LatexRenderer.render(unsafeIcon, output.resolve("icon")) }
    }
}
