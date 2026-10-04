package cv.render.latex

import cv.layout.LayoutManifest
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
 * class and local fonts, and the PDF layout manifest ([LayoutManifest.FILE_NAME])
 * the PDF build verifies the compiled page layout against.
 * [cv.generation.CvApplication] copies content-owned assets such as the
 * profile photo into the tree.
 */
object LatexRenderer : CvRenderer {

    /** Writes `cv.tex`, all section files, the layout manifest, and the bundled template into [outDir]. */
    override fun render(cv: Cv, outDir: Path) {
        val visible = cv.visibleTo(RenderTarget.PDF)
        visible.validateForRendering()
        val layout = LayoutManifest.of(visible).apply { validate() }
        Files.createDirectories(outDir.resolve("sections"))
        LatexTemplate.extractTo(outDir)
        outDir.resolve("cv.tex").toFile().writeText(visible.renderLatexDocument())
        outDir.resolve(LayoutManifest.FILE_NAME).toFile().writeText(layout.render())
        for (section in visible.sections) {
            outDir.resolve("sections/${section.id}.tex").toFile()
                .writeText(section.renderWith(LatexRendererBundle, LatexRenderContext(section.id)))
        }
    }
}
