package cv.testing

import kotlin.test.assertTrue

/**
 * Asserts that a rendered PDF layout report lists [label] on [pages] under
 * [rule], independent of the column padding.
 */
internal fun assertReportRow(report: String, pages: String, rule: String, label: String, violated: Boolean = false) {
    val status = if (violated) "✗" else " "
    val row = Regex("^${Regex.escape(status)} ${Regex.escape(pages)} +${Regex.escape(rule)} +${Regex.escape(label)}$")
    assertTrue(report.lines().any(row::matches), "No row '$pages | $rule | $label' in report:\n$report")
}
