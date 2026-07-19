package cv.render.web

import cv.model.Cv
import cv.model.RenderTarget
import cv.render.CvRenderer
import cv.render.validateForRendering
import cv.render.visibleTo
import java.nio.file.Files
import java.nio.file.Path

/**
 * Generates the reusable static portfolio representation.
 *
 * The output contains a complete `index.html` and bundled browser assets. The
 * generation application places the profile photo named by [Cv.photo] beside
 * them, while site assembly adds `cv.pdf` when producing a deployable site.
 */
object WebRenderer : CvRenderer {

    /** Generates `index.html` and extracts the browser assets into [outDir]. */
    override fun render(cv: Cv, outDir: Path) {
        val visible = cv.visibleTo(RenderTarget.WEB)
        require(visible.sections.isNotEmpty()) { "A web portfolio requires at least one section" }
        visible.validateForRendering()
        Files.createDirectories(outDir)
        outDir.resolve("index.html").toFile().writeText(visible.renderWebDocument())
        WebTemplate.extractTo(outDir)
    }
}
