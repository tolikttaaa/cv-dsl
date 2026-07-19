package cv.render.markdown

import cv.model.Organization
import cv.model.Social
import cv.render.ElementRenderer

/** Renders an organization as an optionally emphasized Markdown link. */
internal fun Organization.renderMarkdown(emphasized: Boolean): String {
    val label = MarkdownText.escape(name)
    val linkedLabel = url?.let { "[$label](${MarkdownText.escapeUrl(it)})" } ?: label
    return if (emphasized) "**$linkedLabel**" else linkedLabel
}

/** Renders tags as code spans separated by middle dots. */
internal fun List<String>.renderMarkdownTags(): String = if (isEmpty()) {
    ""
} else {
    joinToString(separator = " · ") { "`${it.replace("`", "")}`" }
}

/** Renders contact and social entries as Markdown fragments. */
internal object MarkdownSocialRenderer : ElementRenderer<Social, MarkdownRenderContext> {
    override fun render(element: Social, context: MarkdownRenderContext): String = when (element) {
        is Social.Phone -> MarkdownText.escape(element.number)
        is Social.Telegram -> link(
            text = "t.me/${element.handle}",
            url = "https://t.me/${element.handle}",
        )
        is Social.Email -> link(element.address, "mailto:${element.address}")
        is Social.LinkedIn -> link(
            text = "linkedin.com/in/${element.handle}",
            url = "https://www.linkedin.com/in/${element.handle}",
        )
        is Social.LeetCode -> link(
            text = "leetcode.com/u/${element.handle}",
            url = "https://leetcode.com/u/${element.handle}",
        )
        is Social.GitHub -> link(
            text = "github.com/${element.handle}",
            url = "https://github.com/${element.handle}",
        )
        is Social.Address -> MarkdownText.escape(element.text)
    }

    private fun link(text: String, url: String): String =
        "[${MarkdownText.escape(text)}](${MarkdownText.escapeUrl(url)})"
}
