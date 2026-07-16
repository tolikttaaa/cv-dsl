package cv.render

import cv.model.Cv
import java.nio.file.Path

private val safeSectionId = Regex("[A-Za-z0-9][A-Za-z0-9_-]*")
private val safeIcon = Regex("fa[A-Za-z0-9]+")
private val latexLength = Regex("[0-9]+(?:\\.[0-9]+)?(?:pt|mm|cm|in|em|ex)")

/** Rejects values that would escape output directories or inject generated markup. */
internal fun Cv.validateForRendering() {
    val duplicateIds = sections.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
    require(duplicateIds.isEmpty()) { "Section ids must be unique; duplicates: ${duplicateIds.sorted()}" }
    sections.forEach { section ->
        require(section.id.matches(safeSectionId)) {
            "Section id must contain only letters, numbers, '-' and '_': ${section.id}"
        }
        require(section.icon.matches(safeIcon)) {
            "Section icon must be a FontAwesome command such as faSuitcase: ${section.icon}"
        }
    }
    photo?.let {
        val path = Path.of(it.file)
        require(it.file.isNotBlank() && !path.isAbsolute && !path.normalize().startsWith("..")) {
            "CV asset path must be relative and remain inside the output directory: ${it.file}"
        }
        require(it.size.matches(latexLength)) {
            "Photo size must be a positive LaTeX length such as 2.2cm: ${it.size}"
        }
    }
}
