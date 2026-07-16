package cv.generation

import cv.dsl.cv
import cv.testing.sampleCv
import org.junit.jupiter.api.io.TempDir
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CvApplicationTest {
    @TempDir
    lateinit var root: Path

    @Test
    fun `parses generation targets case insensitively`() {
        assertEquals(GenerationTarget.Latex, GenerationTarget.parse("latex"))
        assertEquals(GenerationTarget.Web, GenerationTarget.parse("WEB"))
        assertEquals(GenerationTarget.All, GenerationTarget.parse("All"))
        val error = assertFailsWith<IllegalArgumentException> { GenerationTarget.parse("json") }
        assertTrue(error.message.orEmpty().contains("latex, web or all"))
    }

    @Test
    fun `generates selected output and copies consumer assets`() {
        val copied = mutableListOf<Pair<String, Path>>()
        val reports = mutableListOf<String>()
        val application = CvApplication(
            sampleCv,
            assetSource = CvAssetSource { path, target ->
                copied += path to target
                Files.createDirectories(target.parent)
                target.writeText("image")
            },
            report = reports::add,
        )

        application.generate(root, GenerationTarget.Web)

        assertTrue(Files.isRegularFile(root.resolve("build/web/index.html")))
        assertTrue(Files.isRegularFile(root.resolve("build/web/portrait.png")))
        assertFalse(Files.exists(root.resolve("build/latex")))
        assertEquals("portrait.png", copied.single().first)
        assertEquals(listOf("Generated web portfolio sources in ${root.resolve("build/web")}"), reports)
    }

    @Test
    fun `run defaults to all formats and accepts explicit arguments`() {
        val noPhoto = sampleCv.copy(photo = null)
        CvApplication(noPhoto, report = {}).run(arrayOf(root.toString(), "latex"))
        assertTrue(Files.isRegularFile(root.resolve("build/latex/cv.tex")))
        assertFalse(Files.exists(root.resolve("build/web")))

        val defaultsRoot = root.resolve("defaults")
        CvApplication(noPhoto, report = {}).generate(defaultsRoot)
        assertTrue(Files.isRegularFile(defaultsRoot.resolve("build/latex/cv.tex")))
        assertTrue(Files.isRegularFile(defaultsRoot.resolve("build/web/index.html")))
    }

    @Test
    fun `classpath asset source copies resources and explains missing assets`() {
        val resources = root.resolve("resources")
        Files.createDirectories(resources)
        resources.resolve("photo.bin").writeText("portrait")
        URLClassLoader(arrayOf(resources.toUri().toURL()), null).use { loader ->
            val source = ClasspathCvAssetSource(loader)
            val target = root.resolve("out/nested/photo.bin")
            source.copy("/photo.bin", target)
            assertEquals("portrait", target.readText())

            val error = assertFailsWith<IllegalStateException> {
                source.copy("missing.bin", root.resolve("missing.bin"))
            }
            assertTrue(error.message.orEmpty().contains("/missing.bin not found"))
        }
    }

    @Test
    fun `rejects asset paths outside the generated directory`() {
        val unsafe = cv {
            photo("../secret.png")
            summary("Summary", "faUser") { paragraph("text") }
        }
        assertFailsWith<IllegalArgumentException> {
            CvApplication(unsafe, CvAssetSource { _, _ -> }).generate(root, GenerationTarget.Web)
        }
    }
}
