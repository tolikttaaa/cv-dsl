package cv.gradle

import cv.layout.LayoutElement
import cv.layout.LayoutManifest
import cv.model.PageFit
import cv.testing.assertReportRow
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises the layout verification of [CompileCvPdfTask] with a fake LuaLaTeX
 * that writes predefined page marks, so no TeX installation is needed.
 */
class PdfLayoutVerificationTest {
    @TempDir
    lateinit var directory: Path

    private val manifest = LayoutManifest(
        maxPages = 2,
        elements = listOf(
            LayoutElement("summary", "Summary", PageFit.OnPage(1)),
            LayoutElement("experience", "Experience", PageFit.OnPage(1)),
            LayoutElement("experience/1", "Experience › Engineer, Azul", PageFit.Anywhere),
        ),
    )

    @Test
    fun `passes and reports placements when every rule holds`() {
        val task = compileTask(marks = "mark summary 1\\nmark experience 1\\nmark experience/1 1\\npages 2\\n")
        task.compile()

        val report = task.layoutReportFile.get().asFile.readText()
        assertTrue(report.startsWith("PDF layout: 2 pages, all 3 page rules hold\n"))
        assertReportRow(report, "page 1", "on page 1", "Experience")
    }

    @Test
    fun `fails with every violated rule after writing the PDF and the report`() {
        val task = compileTask(marks = "mark summary 1\\nmark experience 1\\nmark experience/1 2\\npages 3\\n")

        val error = assertFailsWith<GradleException> { task.compile() }
        val message = error.message.orEmpty()
        assertTrue(message.startsWith("PDF layout: 3 pages, 2 of 3 page rules violated:\n"))
        assertTrue(message.contains("  - The PDF has 3 pages, but is limited to 2\n"))
        assertTrue(message.contains("  - Experience must fit on page 1, but occupies pages 1–2\n"))
        assertTrue(message.contains("cv-layout.txt"))
        assertTrue(task.pdfFile.get().asFile.isFile)
        assertReportRow(task.layoutReportFile.get().asFile.readText(), "pages 1–2", "on page 1", "Experience", true)
    }

    @Test
    fun `fails clearly when rules exist but no page marks were recorded`() {
        val task = compileTask(marks = null)

        val error = assertFailsWith<GradleException> { task.compile() }
        assertTrue(error.message.orEmpty().contains("did not record page marks"))
    }

    @Test
    fun `skips verification without a manifest or without rules`() {
        compileTask(marks = null, manifest = null).compile()

        val unconstrained = LayoutManifest(null, listOf(LayoutElement("summary", "Summary", PageFit.Anywhere)))
        val task = compileTask(marks = null, manifest = unconstrained, name = "unconstrained")
        task.compile()
        assertFalse(task.layoutReportFile.get().asFile.exists())
    }

    @Test
    fun `plugin wires the layout report of generatePdf into the build directory`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.resolve("plugin").toFile()).build()
        project.pluginManager.apply(CvGenerationPlugin::class.java)
        project.pluginManager.apply(org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper::class.java)

        val task = project.tasks.getByName("generatePdf") as CompileCvPdfTask
        val expected = project.layout.buildDirectory.file("cv-layout.txt").get().asFile
        assertEquals(expected, task.layoutReportFile.get().asFile)
    }

    /**
     * Registers a [CompileCvPdfTask] whose LuaLaTeX is a shell script that
     * writes `cv.pdf` and, when [marks] is set, `cv.pagemarks` (with `\n`
     * escapes) into the output directory.
     */
    private fun compileTask(
        marks: String?,
        manifest: LayoutManifest? = this.manifest,
        name: String = "compile",
    ): CompileCvPdfTask {
        val project: Project = ProjectBuilder.builder().withProjectDir(directory.resolve(name).toFile()).build()
        val latex = project.layout.buildDirectory.dir("latex").get().asFile.apply { mkdirs() }
        latex.resolve("cv.tex").writeText("\\documentclass{cvdsl}")
        manifest?.let { latex.resolve(LayoutManifest.FILE_NAME).writeText(it.render()) }

        val script = directory.resolve("$name-lualatex").toFile()
        script.writeText(
            buildString {
                appendLine("#!/bin/sh")
                appendLine("for arg in \"$@\"; do")
                appendLine("  case \"${'$'}arg\" in -output-directory=*) out=\"${'$'}{arg#*=}\";; esac")
                appendLine("done")
                appendLine(": > \"${'$'}out/cv.pdf\"")
                marks?.let { appendLine("printf '$it' > \"${'$'}out/cv.pagemarks\"") }
            },
        )
        script.setExecutable(true)

        return project.tasks.register("compileTest", CompileCvPdfTask::class.java).get().apply {
            lualatexExecutable.set(script.absolutePath)
            latexDirectory.set(latex)
            pdfFile.set(project.layout.buildDirectory.file("cv.pdf"))
            logFile.set(project.layout.buildDirectory.file("lualatex.log"))
            layoutReportFile.set(project.layout.buildDirectory.file("cv-layout.txt"))
        }
    }
}
