package cv.render.markdown

import cv.model.Cv
import cv.model.RenderTarget
import cv.render.CvRenderer
import cv.render.validateForRendering
import cv.render.visibleTo
import java.nio.file.Files
import java.nio.file.Path

/** Generates a standalone Markdown representation of a CV. */
object MarkdownRenderer : CvRenderer {

    /** Generates `cv.md` in [outDir]. */
    override fun render(cv: Cv, outDir: Path) {
        val visible = cv.visibleTo(RenderTarget.MARKDOWN)
        require(visible.sections.isNotEmpty()) { "A Markdown CV requires at least one section" }
        visible.validateForRendering()
        Files.createDirectories(outDir)
        outDir.resolve("cv.md").toFile().writeText(visible.renderMarkdownDocument())
    }
}
