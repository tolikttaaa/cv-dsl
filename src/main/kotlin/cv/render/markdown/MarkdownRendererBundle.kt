package cv.render.markdown

import cv.render.RendererBundle

/** Complete Markdown renderer set; every required model renderer is declared here. */
internal object MarkdownRendererBundle : RendererBundle<MarkdownRenderContext> {
    override val summarySectionRenderer = MarkdownSummarySectionRenderer
    override val worksSectionRenderer = MarkdownWorksSectionRenderer
    override val skillsSectionRenderer = MarkdownSkillsSectionRenderer
    override val projectsSectionRenderer = MarkdownProjectsSectionRenderer
    override val educationSectionRenderer = MarkdownEducationSectionRenderer
    override val referencesSectionRenderer = MarkdownReferencesSectionRenderer

    override val workRenderer = MarkdownWorkRenderer
    override val projectRenderer = MarkdownProjectRenderer
    override val skillEntryRenderer = MarkdownSkillEntryRenderer
    override val educationEntryRenderer = MarkdownEducationEntryRenderer
    override val refereeRenderer = MarkdownRefereeRenderer
    override val socialRenderer = MarkdownSocialRenderer
}
