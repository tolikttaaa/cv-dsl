package cv.model

/**
 * Print settings of the PDF representation. The web and Markdown outputs
 * ignore them.
 *
 * @property fontSize Base font size in points, e.g. `9.5`. Every other size of
 *   the document (headings, contacts, keyword chips, …) scales with it. `null`
 *   keeps the document-class default of 10pt.
 * @property maxPages Upper bound for the page count of the compiled PDF; `null`
 *   means unbounded. The `generatePdf` task fails when the PDF is longer.
 */
data class PdfLayout(
    val fontSize: Double? = null,
    val maxPages: Int? = null,
) {
    init {
        fontSize?.let {
            require(it in FONT_SIZE_RANGE) {
                "PDF font size must be between ${FONT_SIZE_RANGE.start} and ${FONT_SIZE_RANGE.endInclusive} pt: $it"
            }
        }
        maxPages?.let { require(it >= 1) { "PDF page limit must be at least 1: $it" } }
    }

    private companion object {
        val FONT_SIZE_RANGE = 6.0..16.0
    }
}

/**
 * Where a CV element must land in the compiled PDF.
 *
 * Page rules are verified, not enforced: LaTeX lays the document out as usual,
 * and the `generatePdf` task fails with a list of every violated rule. Change
 * the content or [PdfLayout.fontSize] until the rules hold. The web and
 * Markdown outputs ignore page rules.
 */
sealed interface PageFit {
    /** No rule: the element may land anywhere, including across a page break. */
    data object Anywhere : PageFit

    /** The whole element must sit on one page, whichever page that is. */
    data object SinglePage : PageFit

    /** The whole element must sit on [page] (1-based). */
    data class OnPage(val page: Int) : PageFit {
        init {
            require(page >= 1) { "PDF page numbers start at 1: $page" }
        }
    }
}
