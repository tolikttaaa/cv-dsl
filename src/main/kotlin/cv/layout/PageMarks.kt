package cv.layout

/**
 * Pages recorded by `\cvpagemark` while LuaLaTeX shipped out the document,
 * parsed from `<jobname>.pagemarks`.
 *
 * @property pageCount Total number of pages, or `null` when the document did
 *   not finish writing the file.
 */
internal class PageMarks(val pageCount: Int?, private val pagesByKey: Map<String, List<Int>>) {

    /**
     * First to last page occupied by the element keyed [key] together with its
     * nested entries, or `null` when nothing of it was marked.
     */
    fun span(key: String): IntRange? {
        val pages = pagesByKey.filterKeys { LayoutKeys.isWithin(it, key) }.values.flatten()
        return if (pages.isEmpty()) null else pages.min()..pages.max()
    }

    companion object {
        /** File extension LuaLaTeX writes the marks under, next to the PDF. */
        const val EXTENSION = "pagemarks"

        private const val MARK = "mark"
        private const val PAGES = "pages"
        private const val MARK_FIELDS = 3
        private const val PAGES_FIELDS = 2

        /** Parses `mark <key> <page>` lines and the trailing `pages <count>` line. */
        fun parse(text: String): PageMarks {
            var pageCount: Int? = null
            val pagesByKey = mutableMapOf<String, MutableList<Int>>()
            for (line in text.lineSequence().map(String::trim).filter(String::isNotEmpty)) {
                val fields = line.split(Regex("\\s+"))
                when {
                    fields.first() == MARK && fields.size == MARK_FIELDS ->
                        pagesByKey.getOrPut(fields[1]) { mutableListOf() } += fields[2].toPage(line)
                    fields.first() == PAGES && fields.size == PAGES_FIELDS -> pageCount = fields[1].toPage(line)
                    else -> malformed(line)
                }
            }
            return PageMarks(pageCount, pagesByKey)
        }

        private fun String.toPage(line: String): Int = toIntOrNull() ?: malformed(line)

        private fun malformed(line: String): Nothing =
            throw IllegalArgumentException("Malformed page mark line: $line")
    }
}
