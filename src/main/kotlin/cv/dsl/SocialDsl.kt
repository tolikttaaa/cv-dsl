package cv.dsl

import cv.model.RenderScope
import cv.model.Social

/**
 * Builder for one visual row of the contact block in the CV header.
 * Entries added here appear side by side on the same line.
 */
@CvDsl
class SocialRowBuilder {
    internal val entries = mutableListOf<Social>()

    /**
     * Adds a phone number.
     *
     * @param scope Render targets the element appears in.
     */
    fun phone(number: String, scope: RenderScope = RenderScope.all) {
        entries += Social.Phone(number, scope)
    }

    /**
     * Adds a Telegram handle (without the `@`).
     *
     * @param scope Render targets the element appears in.
     */
    fun telegram(handle: String, scope: RenderScope = RenderScope.all) {
        entries += Social.Telegram(handle, scope)
    }

    /**
     * Adds an e-mail address.
     *
     * @param scope Render targets the element appears in.
     */
    fun email(address: String, scope: RenderScope = RenderScope.all) {
        entries += Social.Email(address, scope)
    }

    /**
     * Adds a LinkedIn profile id.
     *
     * @param scope Render targets the element appears in.
     */
    fun linkedin(handle: String, scope: RenderScope = RenderScope.all) {
        entries += Social.LinkedIn(handle, scope)
    }

    /**
     * Adds a LeetCode username.
     *
     * @param scope Render targets the element appears in.
     */
    fun leetcode(handle: String, scope: RenderScope = RenderScope.all) {
        entries += Social.LeetCode(handle, scope)
    }

    /**
     * Adds a GitHub username.
     *
     * @param scope Render targets the element appears in.
     */
    fun github(handle: String, scope: RenderScope = RenderScope.all) {
        entries += Social.GitHub(handle, scope)
    }

    /**
     * Adds a free-form location, e.g. a city or country.
     *
     * @param scope Render targets the element appears in.
     */
    fun address(text: String, scope: RenderScope = RenderScope.all) {
        entries += Social.Address(text, scope)
    }
}

/**
 * Builder for the contact block of the CV header: a list of [row]s,
 * each rendered on its own line.
 */
@CvDsl
class SocialBuilder {
    internal val rows = mutableListOf<List<Social>>()

    /** Starts a new line of contact entries. */
    fun row(block: SocialRowBuilder.() -> Unit) {
        rows += SocialRowBuilder().apply(block).entries.toList()
    }
}
