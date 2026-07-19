package cv.render

import cv.dsl.cv
import cv.model.Organization
import cv.model.RenderScope
import cv.model.RenderTarget
import cv.render.latex.LatexRenderer
import cv.render.markdown.MarkdownRenderer
import cv.render.web.WebRenderer
import cv.testing.sampleCv
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RendererIntegrationTest {
    @TempDir
    lateinit var output: Path

    @Test
    fun `factory returns every supported renderer implementation`() {
        assertEquals(WebRenderer, CvRendererFactory.create(RenderFormat.Web))
        assertEquals(LatexRenderer, CvRendererFactory.create(RenderFormat.Latex))
        assertEquals(MarkdownRenderer, CvRendererFactory.create(RenderFormat.Markdown))
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
    fun `renders a complete Markdown document`() {
        val markdown = output.resolve("markdown")
        MarkdownRenderer.render(sampleCv, markdown)

        assertEquals(
            setOf("cv.md"),
            Files.list(markdown).use { files -> files.map { it.fileName.toString() }.toList().toSet() },
        )
        val document = markdown.resolve("cv.md").readText()
        assertTrue(document.startsWith("# Ada Lovelace\n*Computing pioneer & mathematician*"))
        assertTrue(document.contains("[github.com/ada](https://github.com/ada)"))
        assertTrue(document.contains("## About"))
        assertTrue(document.contains("### Mathematician"))
        assertTrue(document.contains("**[Analytical Engines](https://example.com/engine)** · London · 1842 – 1843"))
        assertTrue(document.contains("`Algorithms` · `Mathematics`"))
        assertTrue(document.contains("| Technical | Algorithms, Bernoulli numbers |"))
        assertTrue(document.contains("- **1828 – 1835** — "))
        assertTrue(document.contains("[charles@example.com](mailto:charles@example.com)"))
        assertTrue(document.endsWith("\n"))
    }

    @Test
    fun `markdown rendering requires at least one section and validates content`() {
        val empty = assertFailsWith<IllegalArgumentException> {
            MarkdownRenderer.render(cv {}, output.resolve("md-empty"))
        }
        assertTrue(empty.message.orEmpty().contains("at least one section"))

        val unsafePhoto = cv {
            photo("../portrait.png")
            summary("Summary", "faUser") { paragraph("text") }
        }
        assertFailsWith<IllegalArgumentException> {
            MarkdownRenderer.render(unsafePhoto, output.resolve("md-photo"))
        }
    }

    @Test
    fun `honors per-element render scopes in every format`() {
        val scoped = cv {
            firstName = "Ada"
            lastName = "Lovelace"
            tagline = "Pioneer"
            social {
                row {
                    email("ada@example.com")
                    phone("+44 20 0000 0000", scope = RenderScope.only(RenderTarget.PDF))
                }
            }
            summary("Summary", "faUser") { paragraph("Everywhere") }
            skills("Skills", "faCode", scope = RenderScope.only(RenderTarget.WEB)) {
                entry("Web only", listOf("HTML"))
            }
            experience("Experience", "faSuitcase", id = "experience") {
                work(
                    role = "Mathematician",
                    company = Organization("Engines"),
                    location = "London",
                    dates = "1842",
                    tags = emptyList(),
                    scope = RenderScope.except(RenderTarget.MARKDOWN),
                ) { paragraph("Hidden from Markdown") }
                work(
                    role = "Translator",
                    company = Organization("Journals"),
                    location = "London",
                    dates = "1843",
                    tags = emptyList(),
                ) { paragraph("Visible everywhere") }
            }
        }

        val markdown = output.resolve("scoped-md")
        MarkdownRenderer.render(scoped, markdown)
        val markdownDocument = markdown.resolve("cv.md").readText()
        assertFalse(markdownDocument.contains("Skills"))
        assertFalse(markdownDocument.contains("Mathematician"))
        assertTrue(markdownDocument.contains("Translator"))
        assertFalse(markdownDocument.contains("+44 20 0000 0000"))

        val web = output.resolve("scoped-web")
        WebRenderer.render(scoped, web)
        val html = web.resolve("index.html").readText()
        assertTrue(html.contains("Web only"))
        assertTrue(html.contains("Mathematician"))
        assertFalse(html.contains("+44 20 0000 0000"))

        val latex = output.resolve("scoped-latex")
        LatexRenderer.render(scoped, latex)
        assertFalse(Files.exists(latex.resolve("sections/skills.tex")))
        assertTrue(latex.resolve("cv.tex").readText().contains("+44 20 0000 0000"))
        assertTrue(latex.resolve("sections/experience.tex").readText().contains("Mathematician"))
    }

    @Test
    fun `excluding every section from a target fails clearly`() {
        val markdownless = cv {
            summary("Summary", "faUser", scope = RenderScope.except(RenderTarget.MARKDOWN)) {
                paragraph("text")
            }
        }
        val error = assertFailsWith<IllegalArgumentException> {
            MarkdownRenderer.render(markdownless, output.resolve("no-md-sections"))
        }
        assertTrue(error.message.orEmpty().contains("at least one section"))
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
