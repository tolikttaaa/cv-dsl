package cv.gradle

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/** Consumer-facing configuration for the reusable CV generation tasks. */
abstract class CvGenerationExtension {
    /** Main class that accepts `[root, latex|web|markdown|all]`; defaults to `cv.MainKt`. */
    abstract val mainClass: Property<String>

    /**
     * Render formats produced by the aggregate `generateCv` task: any of
     * `latex`, `web` and `markdown` (`md` is accepted as a Markdown alias).
     * Defaults to all formats. The per-format `generate<Format>` tasks stay
     * registered regardless of this selection.
     */
    abstract val formats: SetProperty<String>

    /** LuaLaTeX executable name or absolute path. */
    abstract val lualatexExecutable: Property<String>

    /** Local port used by `serveSite`. */
    abstract val previewPort: Property<Int>
}
