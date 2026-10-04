package cv.render.latex

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Typesets fragments with the bundled `cvdsl.cls` and the real LuaLaTeX.
 * Skipped when no LuaLaTeX is installed (as on the default CI runner).
 */
class CvdslClassTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `a link keeps the space that follows it`() {
        val widths = measure(
            "link" to """\link{https://example.com}{Link} text""",
            "plain" to "Link text",
        )

        assertEquals(widths.getValue("plain"), widths.getValue("link"))
    }

    /** Widths of each fragment typeset in an `\hbox`, as printed by TeX. */
    private fun measure(vararg fragments: Pair<String, String>): Map<String, String> {
        val lualatex = listOf("/Library/TeX/texbin/lualatex", "lualatex").firstOrNull(::isRunnable)
        assumeTrue(lualatex != null, "LuaLaTeX is not installed")

        LatexTemplate.extractTo(directory)
        directory.resolve("measure.tex").writeText(
            buildString {
                appendLine("""\documentclass[localFont]{cvdsl}""")
                appendLine("""\begin{document}""")
                for ((name, fragment) in fragments) {
                    appendLine("""\setbox0\hbox{$fragment}\typeout{WIDTH $name=\the\wd0}""")
                }
                appendLine("""x""")
                appendLine("""\end{document}""")
            },
        )
        val process = ProcessBuilder(lualatex, "-interaction=nonstopmode", "measure.tex")
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        assertEquals(0, process.waitFor(), output)
        return Regex("""^WIDTH (\w+)=(\S+)$""", RegexOption.MULTILINE).findAll(output)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun isRunnable(executable: String): Boolean = runCatching {
        val process = ProcessBuilder(executable, "--version").redirectErrorStream(true)
            .redirectOutput(File.createTempFile("lualatex", ".version")).start()
        process.waitFor() == 0
    }.getOrDefault(false)
}
