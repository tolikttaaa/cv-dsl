package cv.model

/**
 * A single contact / social entry shown in the CV header.
 *
 * Each variant knows only its raw value (a handle, a number, …); the renderers
 * derive the presentation from the type — the LaTeX renderer maps each variant
 * to the matching `cvdsl.cls` command (`\smartphone`, `\telegram`, …)
 * and the web renderer derives the icon, the display text, and the target URL.
 */
sealed interface Social {
    /** Render targets this contact entry appears in. */
    val scope: RenderScope

    /** Phone number, rendered verbatim. */
    data class Phone(
        val number: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** Telegram handle without the `@` (linked to `t.me/<handle>`). */
    data class Telegram(
        val handle: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** E-mail address (linked as `mailto:`). */
    data class Email(
        val address: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** LinkedIn profile id (linked to `linkedin.com/in/<handle>`). */
    data class LinkedIn(
        val handle: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** LeetCode username (linked to `leetcode.com/u/<handle>`). */
    data class LeetCode(
        val handle: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** GitHub username (linked to `github.com/<handle>`). */
    data class GitHub(
        val handle: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social

    /** Free-form location text, e.g. a city or country. */
    data class Address(
        val text: String,
        override val scope: RenderScope = RenderScope.all,
    ) : Social
}
