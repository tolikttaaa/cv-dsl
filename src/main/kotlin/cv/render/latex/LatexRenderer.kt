package cv.render.latex

import cv.model.Cv
import cv.model.RenderTarget
import cv.render.CvRenderer
import cv.render.renderWith
import cv.render.validateForRendering
import cv.render.visibleTo
import java.nio.file.Files
import java.nio.file.Path

/**
 * Generates a compilable LaTeX source tree.
 *
 * The output contains `cv.tex`, one file per section, the `cvdsl` document
 * class and local fonts. [cv.generation.CvApplication] copies content-owned
 * assets such as the profile photo into the tree.
 */
object LatexRenderer : CvRenderer {

    /** Writes `cv.tex`, all section files, and the bundled template into [outDir]. */
    override fun render(cv: Cv, outDir: Path) {
        val visible = cv.visibleTo(RenderTarget.PDF)
        visible.validateForRendering()
        Files.createDirectories(outDir.resolve("sections"))
        LatexTemplate.extractTo(outDir)
        outDir.resolve("cv.tex").toFile().writeText(visible.renderLatexDocument())
        for (section in visible.sections) {
            outDir.resolve("sections/${section.id}.tex").toFile()
                .writeText(section.renderWith(LatexRendererBundle, Unit))
        }
    }
}
