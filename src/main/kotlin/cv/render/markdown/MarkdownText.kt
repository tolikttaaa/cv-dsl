package cv.render.markdown

import cv.model.Bold
import cv.model.Bullets
import cv.model.Colored
import cv.model.Description
import cv.model.Inline
import cv.model.Italic
import cv.model.Link
import cv.model.NoWrap
import cv.model.Paragraph
import cv.model.Plain
import cv.model.RichText

/** Converts inline and block markup to safe Markdown fragments. */
internal object MarkdownText {

    /** Renders block content with blank lines between neighboring blocks. */
    fun markdown(description: Description): String =
        description.blocks.joinToString(separator = "\n\n") { block ->
            when (block) {
                is Paragraph -> markdown(block.text)
                is Bullets -> block.items.joinToString(separator = "\n") { "- ${markdown(it)}" }
            }
        }.trim()

    /** Renders a [RichText] run as a normalized Markdown fragment. */
    fun markdown(text: RichText): String = markdown(text.inlines).normalizeSpace()

    private fun markdown(items: List<Inline>): String =
        items.joinToString(separator = "") { markdown(it) }

    private fun markdown(node: Inline): String = when (node) {
        is Plain -> escape(node.text)
        is Bold -> "**${markdown(node.content)}**"
        is Italic -> "*${markdown(node.content)}*"
        is Colored -> markdown(node.content)
        is Link -> "[${markdown(node.content)}](${escapeUrl(node.url)})"
        is NoWrap -> markdown(node.content)
    }

    /** Renders a [RichText] run as normalized plain text, dropping all styling. */
    fun plain(text: RichText): String = plain(text.inlines).normalizeSpace()

    private fun plain(items: List<Inline>): String =
        items.joinToString(separator = "") { node ->
            when (node) {
                is Plain -> node.text
                is Bold -> plain(node.content)
                is Italic -> plain(node.content)
                is Colored -> plain(node.content)
                is Link -> plain(node.content)
                is NoWrap -> plain(node.content)
            }
        }

    /** Escapes Markdown-significant characters in plain text. */
    fun escape(s: String): String = buildString {
        for (character in s) {
            when (character) {
                '\\', '`', '*', '_', '[', ']', '<', '>', '#', '|' -> append('\\').append(character)
                else -> append(character)
            }
        }
    }

    /** Escapes characters that can break a Markdown link destination. */
    fun escapeUrl(url: String): String = url
        .replace(" ", "%20")
        .replace("(", "\\(")
        .replace(")", "\\)")

    private fun String.normalizeSpace(): String = replace(Regex("\\s+"), " ").trim()
}
