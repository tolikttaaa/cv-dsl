package cv.layout

import cv.model.Cv
import cv.model.EducationSection
import cv.model.PageFit
import cv.model.ProjectsSection
import cv.model.ReferencesSection
import cv.model.Section
import cv.model.SkillsSection
import cv.model.SummarySection
import cv.model.WorksSection

/**
 * One element of the PDF whose pages are tracked: a section or one of its
 * entries. [key] is the page-mark key the LaTeX renderer emits for it.
 */
internal data class LayoutElement(val key: String, val label: String, val pageFit: PageFit)

/** Page-mark keys shared by the LaTeX renderer and the layout verification. */
internal object LayoutKeys {
    /**
     * Key of the entry at [index] (0-based) of the element keyed [parent].
     * Entry keys extend their section key, so an element's pages include the
     * pages of everything nested in it.
     */
    fun entry(parent: String, index: Int): String = "$parent/${index + 1}"

    /** Whether [key] identifies [ancestor] itself or an element nested in it. */
    fun isWithin(key: String, ancestor: String): Boolean = key == ancestor || key.startsWith("$ancestor/")
}

/**
 * The PDF layout rules of one generated document: the page limit and every
 * tracked element with its [PageFit]. The LaTeX renderer writes it next to the
 * sources as [FILE_NAME]; the PDF build reads it back to verify the compiled
 * document against the page marks LuaLaTeX recorded.
 */
internal data class LayoutManifest(val maxPages: Int?, val elements: List<LayoutElement>) {
    /** Whether the document declares any rule that needs verification. */
    val hasRules: Boolean
        get() = maxPages != null || elements.any { it.pageFit != PageFit.Anywhere }

    /** Rejects rules that no layout could satisfy. */
    fun validate() {
        val fits = elements.associate { it.key to it.pageFit }
        for (element in elements) {
            val page = (element.pageFit as? PageFit.OnPage)?.page ?: continue
            maxPages?.let { max ->
                require(page <= max) {
                    "${element.label} must be on page $page, but the PDF is limited to ${pages(max)}"
                }
            }
            val parent = element.key.substringBeforeLast('/', missingDelimiterValue = "")
            val parentPage = (fits[parent] as? PageFit.OnPage)?.page
            require(parentPage == null || parentPage == page) {
                "${element.label} must be on page $page, but its section must be on page $parentPage"
            }
        }
    }

    /** Serializes the manifest as tab-separated lines. */
    fun render(): String = buildString {
        appendLine(HEADER)
        maxPages?.let { appendLine("$MAX_PAGES\t$it") }
        for (element in elements) {
            val fields = listOf(ELEMENT, element.key, element.pageFit.code, element.label.singleLine())
            appendLine(fields.joinToString("\t"))
        }
    }

    companion object {
        /** File name of the manifest inside the generated LaTeX directory. */
        const val FILE_NAME = "layout.tsv"

        private const val HEADER = "# cv-dsl PDF layout manifest: page limit, tracked elements and their page rules"
        private const val MAX_PAGES = "max-pages"
        private const val ELEMENT = "element"
        private const val ELEMENT_FIELDS = 4
        private const val ON_PAGE_PREFIX = "page:"

        /** Lists the tracked elements of [cv], which must already be filtered for the PDF. */
        fun of(cv: Cv): LayoutManifest = LayoutManifest(
            maxPages = cv.pdf.maxPages,
            elements = cv.sections.flatMap { section ->
                val sectionElement = LayoutElement(section.id, section.title, section.pageFit)
                listOf(sectionElement) + section.entryRules().mapIndexed { index, (label, fit) ->
                    LayoutElement(LayoutKeys.entry(section.id, index), "${section.title} › $label", fit)
                }
            },
        )

        /** Parses a manifest produced by [render]. */
        fun parse(text: String): LayoutManifest {
            var maxPages: Int? = null
            val elements = mutableListOf<LayoutElement>()
            for (line in text.lineSequence().filter { it.isNotBlank() && !it.startsWith("#") }) {
                val fields = line.split('\t')
                when (fields.first()) {
                    MAX_PAGES -> maxPages = fields.getOrNull(1)?.toIntOrNull() ?: malformed(line)
                    ELEMENT -> {
                        if (fields.size != ELEMENT_FIELDS) malformed(line)
                        val (key, code, label) = fields.drop(1)
                        elements += LayoutElement(key, label, parsePageFit(code) ?: malformed(line))
                    }
                    else -> malformed(line)
                }
            }
            return LayoutManifest(maxPages, elements)
        }

        private val PageFit.code: String
            get() = when (this) {
                PageFit.Anywhere -> "anywhere"
                PageFit.SinglePage -> "single-page"
                is PageFit.OnPage -> "$ON_PAGE_PREFIX$page"
            }

        private fun parsePageFit(code: String): PageFit? = when {
            code == "anywhere" -> PageFit.Anywhere
            code == "single-page" -> PageFit.SinglePage
            code.startsWith(ON_PAGE_PREFIX) ->
                code.removePrefix(ON_PAGE_PREFIX).toIntOrNull()?.takeIf { it >= 1 }?.let(PageFit::OnPage)
            else -> null
        }

        private fun malformed(line: String): Nothing =
            throw IllegalArgumentException("Malformed PDF layout manifest line: $line")

        private fun String.singleLine(): String = replace(Regex("\\s+"), " ").trim()

        /** Label and page rule of each entry, in rendering order; the summary has no entries. */
        private fun Section.entryRules(): List<Pair<String, PageFit>> = when (this) {
            is SummarySection -> emptyList()
            is WorksSection -> works.map { "${it.role}, ${it.company.name}" to it.pageFit }
            is SkillsSection -> entries.map { it.category to it.pageFit }
            is ProjectsSection -> projects.map { it.name to it.pageFit }
            is EducationSection -> entries.map { it.years to it.pageFit }
            is ReferencesSection -> referees.map { it.name to it.pageFit }
        }
    }
}
