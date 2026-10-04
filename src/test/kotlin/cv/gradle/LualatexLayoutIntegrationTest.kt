package cv.gradle

import cv.dsl.cv
import cv.model.Organization
import cv.model.PageFit
import cv.render.latex.LatexRenderer
import cv.testing.assertReportRow
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Compiles a generated document with the real LuaLaTeX and verifies that the
 * page marks of `cvdsl.cls` report where elements landed. Skipped when no
 * LuaLaTeX is installed (as on the default CI runner).
 */
class LualatexLayoutIntegrationTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `page marks of the compiled PDF drive the layout verification`() {
        val lualatex = listOf("/Library/TeX/texbin/lualatex", "lualatex").firstOrNull(::isRunnable)
        assumeTrue(lualatex != null, "LuaLaTeX is not installed")

        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val latex = project.layout.buildDirectory.dir("latex").get().asFile
        LatexRenderer.render(longCv, latex.toPath())
        val task = project.tasks.register("compileTest", CompileCvPdfTask::class.java).get().apply {
            lualatexExecutable.set(lualatex)
            latexDirectory.set(latex)
            pdfFile.set(project.layout.buildDirectory.file("cv.pdf"))
            logFile.set(project.layout.buildDirectory.file("lualatex.log"))
            layoutReportFile.set(project.layout.buildDirectory.file("cv-layout.txt"))
        }

        val error = runCatching { task.compile() }.exceptionOrNull()

        val report = task.layoutReportFile.get().asFile.readText()
        assertTrue(error is GradleException, "Expected a layout violation, got $error\n$report")
        val message = error.message.orEmpty()
        assertTrue(message.contains("Experience must fit on a single page, but spans pages 1–2"), message)
        assertReportRow(report, "page 1", "on page 1", "Summary")
        assertReportRow(report, "pages 1–2", "single page", "Experience", violated = true)
        assertReportRow(report, "page 1", "-", "Experience › Role 1, Company 1")
        assertReportRow(report, "page 2", "-", "Experience › Role 12, Company 12")
        assertReportRow(report, "page 2", "on page 2", "Skills")
    }

    /** Long enough for Experience to spill onto page 2, which breaks its single-page rule. */
    private val longCv = cv {
        firstName = "Ada"
        lastName = "Lovelace"
        tagline = "Pioneer"
        footerText = "Ada Lovelace — CV"
        pdf {
            fontSize = 9.5
            maxPages = 2
        }
        summary("Summary", "faUser", pageFit = PageFit.OnPage(1)) {
            paragraph("Mathematician and writer.")
        }
        experience("Experience", "faSuitcase", id = "experience", pageFit = PageFit.SinglePage) {
            (1..12).forEach { index ->
                work("Role $index", Organization("Company $index"), "London", "18$index", listOf("Tag $index")) {
                    paragraph("A position description that is long enough to take a couple of lines. ".repeat(4))
                    bullets { item("An achievement.") }
                }
            }
        }
        skills("Skills", "faCode", pageFit = PageFit.OnPage(2)) {
            entry("Technical", listOf("Algorithms", "Mathematics"))
        }
    }

    private fun isRunnable(executable: String): Boolean = runCatching {
        val process = ProcessBuilder(executable, "--version").redirectErrorStream(true)
            .redirectOutput(File.createTempFile("lualatex", ".version")).start()
        process.waitFor() == 0
    }.getOrDefault(false)
}
