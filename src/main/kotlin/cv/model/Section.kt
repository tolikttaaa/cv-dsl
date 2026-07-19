package cv.model

/**
 * A titled section of the CV.
 *
 * There is one implementation per section layout; the renderers dispatch on the
 * concrete type to produce the matching LaTeX or HTML layout.
 */
sealed interface Section {
    /** Stable identifier used by generated LaTeX files and web navigation. */
    val id: String

    /** Section heading in the PDF. */
    val title: String

    /** Section heading on the web page (defaults to [title] when built through the DSL). */
    val webTitle: String

    /** FontAwesome icon command used by `\sectionTitle`, e.g. `"faSuitcase"`. */
    val icon: String

    /** Render targets this section appears in. */
    val scope: RenderScope
}

/** Free-form introduction text at the top of the CV. */
data class SummarySection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val text: Description,
    /** Render targets this summary appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section

/**
 * An organization a position or project belongs to. Renderers always emphasize
 * the [name] in bold and wrap it in a hyperlink when [url] is present.
 */
data class Organization(val name: String, val url: String? = null)

/** One employment (or teaching) position inside a [WorksSection]. */
data class Work(
    val role: String,
    val company: Organization,
    val location: String,
    val dates: String,
    val description: Description,
    val tags: List<String>,
    /** Render targets this position appears in. */
    val scope: RenderScope = RenderScope.all,
)

/** A chronological list of positions — used for both work experience and teaching. */
data class WorksSection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val works: List<Work>,
    /** Render targets this section appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section

/** One row of the skills table: a category and the skills belonging to it. */
data class SkillEntry(
    val category: String,
    val skills: List<String>,
    /** Render targets this skills row appears in. */
    val scope: RenderScope = RenderScope.all,
)

/** A two-column keyword table of skill categories. */
data class SkillsSection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val entries: List<SkillEntry>,
    /** Render targets this section appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section

/** One personal project inside a [ProjectsSection]. */
data class Project(
    val name: String,
    val company: Organization,
    val dates: String,
    val description: Description,
    val tags: List<String>,
    /** Render targets this project appears in. */
    val scope: RenderScope = RenderScope.all,
)

/** A list of personal / side projects. */
data class ProjectsSection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val projects: List<Project>,
    /** Render targets this section appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section

/** One education milestone: a year range and its rich-text description. */
data class EducationEntry(
    val years: String,
    val description: RichText,
    /** Render targets this milestone appears in. */
    val scope: RenderScope = RenderScope.all,
)

/** A timeline of education milestones. */
data class EducationSection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val entries: List<EducationEntry>,
    /** Render targets this section appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section

/** A single professional reference. */
data class Referee(
    val name: String,
    val role: String,
    val company: Organization,
    val period: String,
    val email: String,
    /** Render targets this reference appears in. */
    val scope: RenderScope = RenderScope.all,
)

/** A list of professional references with contact details. */
data class ReferencesSection(
    override val id: String,
    override val title: String,
    override val webTitle: String,
    override val icon: String,
    val referees: List<Referee>,
    /** Render targets this section appears in. */
    override val scope: RenderScope = RenderScope.all,
) : Section
