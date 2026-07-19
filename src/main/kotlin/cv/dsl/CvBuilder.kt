package cv.dsl

import cv.model.Cv
import cv.model.EducationSection
import cv.model.Photo
import cv.model.ProjectsSection
import cv.model.ReferencesSection
import cv.model.RenderScope
import cv.model.Section
import cv.model.SkillsSection
import cv.model.Social
import cv.model.SummarySection
import cv.model.WorksSection

/**
 * Root builder of the CV DSL.
 *
 * Header fields are set as properties; the contact block via [social];
 * sections are appended in call order by the section methods
 * ([summary], [experience], [skills], [projects], [education], [references]).
 *
 * Every section method takes:
 *  - `title` — the heading in the PDF,
 *  - `icon` — the FontAwesome command for `\sectionTitle` (e.g. `"faSuitcase"`),
 *  - `id` — the stable LaTeX file / web navigation identifier (defaulted where unambiguous),
 *  - `webTitle` — the heading on the web page, when it differs from `title`.
 */
@CvDsl
class CvBuilder {
    /** Given name in display case (uppercased for the PDF header by the LaTeX renderer). */
    var firstName: String = ""

    /** Family name in display case. */
    var lastName: String = ""

    /** Short professional title shown under the name. */
    var tagline: String = ""

    /** Text placed in the page footer of the PDF. */
    var footerText: String = ""

    /**
     * Whether the PDF may hyphenate words across lines. Set to `false` to keep
     * every word whole document-wide instead of marking individual words with
     * [TextBuilder.nowrap].
     */
    var hyphenation: Boolean = true

    private var photo: Photo? = null
    private var social: List<List<Social>> = emptyList()
    private val sections = mutableListOf<Section>()

    /**
     * Adds a profile photo to the header. Optional — without this call the
     * header is rendered without a photo.
     *
     * @param file Image file name, relative to the LaTeX build directory.
     * @param size Rendered photo width as a LaTeX length.
     */
    fun photo(file: String, size: String = "2.2cm") {
        photo = Photo(file, size)
    }

    /** Defines the contact block of the header as rows of entries. */
    fun social(block: SocialBuilder.() -> Unit) {
        social = SocialBuilder().apply(block).rows.toList()
    }

    /**
     * Adds a summary (free-form introduction) section.
     *
     * @param scope Render targets the section appears in.
     */
    fun summary(
        title: String,
        icon: String,
        id: String = "summary",
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: DescriptionBuilder.() -> Unit,
    ) {
        sections += SummarySection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            text = DescriptionBuilder().apply(block).build(),
            scope = scope,
        )
    }

    /**
     * Adds a section listing positions — used for both work experience and teaching.
     *
     * @param scope Render targets the section appears in.
     */
    fun experience(
        title: String,
        icon: String,
        id: String,
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: WorksBuilder.() -> Unit,
    ) {
        sections += WorksSection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            works = WorksBuilder().apply(block).works.toList(),
            scope = scope,
        )
    }

    /**
     * Adds a skills table section.
     *
     * @param scope Render targets the section appears in.
     */
    fun skills(
        title: String,
        icon: String,
        id: String = "skills",
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: SkillsBuilder.() -> Unit,
    ) {
        sections += SkillsSection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            entries = SkillsBuilder().apply(block).entries.toList(),
            scope = scope,
        )
    }

    /**
     * Adds a personal-projects section.
     *
     * @param scope Render targets the section appears in.
     */
    fun projects(
        title: String,
        icon: String,
        id: String = "projects",
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: ProjectsBuilder.() -> Unit,
    ) {
        sections += ProjectsSection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            projects = ProjectsBuilder().apply(block).projects.toList(),
            scope = scope,
        )
    }

    /**
     * Adds an education timeline section.
     *
     * @param scope Render targets the section appears in.
     */
    fun education(
        title: String,
        icon: String,
        id: String = "education",
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: EducationBuilder.() -> Unit,
    ) {
        sections += EducationSection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            entries = EducationBuilder().apply(block).entries.toList(),
            scope = scope,
        )
    }

    /**
     * Adds a references section.
     *
     * @param scope Render targets the section appears in.
     */
    fun references(
        title: String,
        icon: String,
        id: String = "references",
        webTitle: String = title,
        scope: RenderScope = RenderScope.all,
        block: ReferencesBuilder.() -> Unit,
    ) {
        sections += ReferencesSection(
            id = id,
            title = title,
            webTitle = webTitle,
            icon = icon,
            referees = ReferencesBuilder().apply(block).referees.toList(),
            scope = scope,
        )
    }

    /** Finalizes the builder into an immutable [Cv]. */
    fun build() = Cv(
        firstName = firstName,
        lastName = lastName,
        tagline = tagline,
        photo = photo,
        footerText = footerText,
        hyphenation = hyphenation,
        social = social,
        sections = sections.toList(),
    )
}

/** Entry point of the CV DSL: builds a complete [Cv] document. */
fun cv(block: CvBuilder.() -> Unit): Cv = CvBuilder().apply(block).build()
