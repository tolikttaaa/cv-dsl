package cv.render.markdown

import cv.model.Cv
import cv.render.CvRenderer
import cv.render.validateForRendering
import java.nio.file.Files
import java.nio.file.Path

/** Generates a standalone Markdown representation of a CV. */
object MarkdownRenderer : CvRenderer {

    /** Generates `cv.md` in [outDir]. */
    override fun render(cv: Cv, outDir: Path) {
        require(cv.sections.isNotEmpty()) { "A Markdown CV requires at least one section" }
        cv.validateForRendering()
        Files.createDirectories(outDir)
        outDir.resolve("cv.md").toFile().writeText(cv.renderMarkdownDocument())
    }
}
