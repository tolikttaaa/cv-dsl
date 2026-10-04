package cv.dsl

import cv.model.PdfLayout

/**
 * Builder for the print settings of the PDF:
 *
 * ```kotlin
 * pdf {
 *     fontSize = 9.5 // pt; headings, contacts and chips scale with it
 *     maxPages = 2   // generatePdf fails when the PDF grows longer
 * }
 * ```
 *
 * Per-element page rules are declared next to the elements themselves through
 * their `pageFit` parameter.
 */
@CvDsl
class PdfLayoutBuilder {
    /** Base font size in points; `null` keeps the 10pt default. */
    var fontSize: Double? = null

    /** Maximum page count of the compiled PDF; `null` means unbounded. */
    var maxPages: Int? = null

    internal fun build() = PdfLayout(fontSize = fontSize, maxPages = maxPages)
}
