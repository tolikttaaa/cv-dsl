package cv.gradle

import cv.model.RenderTarget
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/** Consumer-facing configuration for the reusable CV generation tasks. */
abstract class CvGenerationExtension {
    /** Main class that accepts `[root, latex|web|markdown|all]`; defaults to `cv.MainKt`. */
    abstract val mainClass: Property<String>

    /**
     * Render targets produced by the aggregate `generateCv` task; defaults to
     * all of [RenderTarget]. [RenderTarget.WEB] builds the deployable site,
     * which embeds the PDF and therefore also compiles it. The per-format
     * `generate<Format>` tasks stay registered regardless of this selection.
     */
    abstract val formats: SetProperty<RenderTarget>

    /** LuaLaTeX executable name or absolute path. */
    abstract val lualatexExecutable: Property<String>

    /** Local port used by `serveSite`. */
    abstract val previewPort: Property<Int>
}
