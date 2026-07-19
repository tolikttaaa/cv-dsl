package cv.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RenderScopeTest {
    @Test
    fun `default scope includes every target`() {
        RenderTarget.entries.forEach { assertTrue(RenderScope.all.includes(it)) }
    }

    @Test
    fun `only limits visibility to the listed targets`() {
        val scope = RenderScope.only(RenderTarget.WEB, RenderTarget.PDF)
        assertTrue(scope.includes(RenderTarget.WEB))
        assertTrue(scope.includes(RenderTarget.PDF))
        assertFalse(scope.includes(RenderTarget.MARKDOWN))
    }

    @Test
    fun `except hides the listed targets and exclusion wins over inclusion`() {
        val scope = RenderScope.except(RenderTarget.MARKDOWN)
        assertTrue(scope.includes(RenderTarget.WEB))
        assertFalse(scope.includes(RenderTarget.MARKDOWN))

        val contradictory = RenderScope(
            renderers = setOf(RenderTarget.WEB),
            excludedRenderers = setOf(RenderTarget.WEB),
        )
        assertFalse(contradictory.includes(RenderTarget.WEB))
    }
}
