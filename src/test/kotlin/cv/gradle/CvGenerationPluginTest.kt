package cv.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CvGenerationPluginTest {
    @Test
    fun `creates extension defaults before Kotlin is applied`() {
        val project = project()
        project.pluginManager.apply(CvGenerationPlugin::class.java)

        val extension = project.extensions.getByType(CvGenerationExtension::class.java)
        assertEquals("cv.MainKt", extension.mainClass.get())
        assertEquals(8080, extension.previewPort.get())
        assertFalse(project.tasks.names.contains("generateWeb"))
    }

    @Test
    fun `registers the complete task pipeline after Kotlin is applied`() {
        val project = project()
        project.pluginManager.apply(CvGenerationPlugin::class.java)
        project.pluginManager.apply(KotlinPluginWrapper::class.java)

        val expected = setOf(
            "verifyCvEnvironment",
            "generateLatex",
            "generateWeb",
            "generatePdf",
            "assembleSite",
            "serveSite",
            "stopSite",
        )
        assertTrue(project.tasks.names.containsAll(expected))
        assertEquals("cv", project.tasks.getByName("generateWeb").group)
        assertNotNull(project.extensions.findByName("cvGeneration"))
    }

    @Test
    fun `environment verification reports a missing LaTeX executable`() {
        val project = project()
        val task = project.tasks.register("verifyTest", VerifyCvEnvironmentTask::class.java).get()
        task.lualatexExecutable.set("definitely-not-a-real-lualatex-command")

        val error = assertFailsWith<GradleException> { task.verify() }
        assertTrue(error.message.orEmpty().contains("LuaLaTeX is unavailable"))
        assertTrue(error.message.orEmpty().contains("-PlualatexPath"))
    }

    @Test
    fun `PDF task fails clearly when generated entry point is absent`() {
        val project = project()
        val latex = project.layout.buildDirectory.dir("test-latex")
        val task = project.tasks.register("compileTest", CompileCvPdfTask::class.java).get().apply {
            lualatexExecutable.set("lualatex")
            latexDirectory.set(latex)
            pdfFile.set(project.layout.buildDirectory.file("test.pdf"))
            logFile.set(project.layout.buildDirectory.file("test.log"))
        }
        Files.createDirectories(latex.get().asFile.toPath())

        val error = assertFailsWith<GradleException> { task.compile() }
        assertTrue(error.message.orEmpty().contains("cv.tex"))
    }

    @Test
    fun `stop helper handles missing malformed and live pid files`() {
        val directory = Files.createTempDirectory("cv-stop-test")
        val pidFile = directory.resolve("server.pid")
        assertFalse(stopRecordedProcess(pidFile.toFile()))

        pidFile.writeText("not-a-pid")
        assertFalse(stopRecordedProcess(pidFile.toFile()))
        assertFalse(Files.exists(pidFile))

        val process = ProcessBuilder("sh", "-c", "sleep 30").start()
        try {
            pidFile.writeText(process.pid().toString())
            assertTrue(stopRecordedProcess(pidFile.toFile()))
            assertFalse(process.isAlive)
            assertFalse(Files.exists(pidFile))
        } finally {
            process.destroyForcibly()
        }
    }

    private fun project(): Project = ProjectBuilder.builder().build()
}
