package cv.layout

import cv.model.PageFit

/** Where one tracked element landed: [pages] is `null` when it was not marked. */
internal data class LayoutPlacement(val element: LayoutElement, val pages: IntRange?) {
    /** The violated rule as a sentence, or `null` when the placement satisfies it. */
    val violation: String?
        get() = when (val fit = element.pageFit) {
            PageFit.Anywhere -> null
            PageFit.SinglePage -> when {
                pages == null -> "${element.label} must fit on a single page, but it is missing from the PDF"
                pages.first != pages.last -> "${element.label} must fit on a single page, but spans ${pages.describe()}"
                else -> null
            }
            is PageFit.OnPage -> when {
                pages == null -> "${element.label} must be on page ${fit.page}, but it is missing from the PDF"
                pages != fit.page..fit.page ->
                    "${element.label} must fit on page ${fit.page}, but occupies ${pages.describe()}"
                else -> null
            }
        }
}

/**
 * The compiled PDF checked against its [LayoutManifest]: the page count, where
 * every tracked element landed, and the rules that do not hold.
 */
internal class PdfLayoutReport(
    val pageCount: Int?,
    val maxPages: Int?,
    val placements: List<LayoutPlacement>,
) {
    /** Every violated rule, page limit first. */
    val violations: List<String>
        get() = listOfNotNull(pageLimitViolation) + placements.mapNotNull { it.violation }

    private val pageLimitViolation: String?
        get() = when {
            maxPages == null -> null
            pageCount == null -> "The PDF is limited to ${pages(maxPages)}, but its page count was not recorded"
            pageCount > maxPages -> "The PDF has $pageCount pages, but is limited to $maxPages"
            else -> null
        }

    private val ruleCount: Int
        get() = (if (maxPages != null) 1 else 0) + placements.count { it.element.pageFit != PageFit.Anywhere }

    /** One-line outcome for the build log. */
    val summary: String
        get() {
            val length = pageCount?.let(::pages) ?: "unknown page count"
            val failed = violations.size
            return when {
                ruleCount == 0 -> "PDF layout: $length, no page rules declared"
                failed == 0 -> "PDF layout: $length, all $ruleCount page rules hold"
                else -> "PDF layout: $length, $failed of $ruleCount page rules violated"
            }
        }

    /** Human-readable report: the summary, then one line per tracked element. */
    fun render(): String = buildString {
        appendLine(summary)
        violations.forEach { appendLine("  ✗ $it") }
        appendLine()
        val rows = placements.map { placement ->
            Triple(placement.pages?.describe() ?: "missing", placement.element.pageFit.describe(), placement)
        }
        val pagesWidth = rows.maxOfOrNull { it.first.length } ?: 0
        val ruleWidth = rows.maxOfOrNull { it.second.length } ?: 0
        for ((pages, rule, placement) in rows) {
            val status = if (placement.violation == null) " " else "✗"
            appendLine("$status ${pages.padEnd(pagesWidth)}  ${rule.padEnd(ruleWidth)}  ${placement.element.label}")
        }
    }

    companion object {
        /** Locates every element of [manifest] in [marks]. */
        fun of(manifest: LayoutManifest, marks: PageMarks) = PdfLayoutReport(
            pageCount = marks.pageCount,
            maxPages = manifest.maxPages,
            placements = manifest.elements.map { LayoutPlacement(it, marks.span(it.key)) },
        )
    }
}

/** [count] with the matching form of "page": `1 page`, `2 pages`. */
internal fun pages(count: Int): String = if (count == 1) "1 page" else "$count pages"

private fun IntRange.describe(): String = if (first == last) "page $first" else "pages $first–$last"

private fun PageFit.describe(): String = when (this) {
    PageFit.Anywhere -> "-"
    PageFit.SinglePage -> "single page"
    is PageFit.OnPage -> "on page $page"
}
