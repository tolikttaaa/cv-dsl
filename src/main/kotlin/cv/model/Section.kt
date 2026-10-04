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

    /** Where the whole section, title included, must land in the PDF. */
    val pageFit: PageFit
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
    override val pageFit: PageFit = PageFit.Anywhere,
) : Section

/**
 * An organization a position or project belongs to. Renderers always emphasize
 * the [name] in bold and wrap it in a hyperlink when [url] is present.
 *
 * @property host Code-hosting platform the organization is a repository on;
 *   renderers append it to the name with the platform's icon, inside the link.
 */
data class Organization(val name: String, val url: String? = null, val host: CodeHost? = null) {
    companion object {
        /**
         * The [repository] of [owner] on [host], labeled with the repository
         * name and linked to it, e.g. `repository(CodeHost.GITHUB, "ada", "engine")`
         * for `https://github.com/ada/engine`.
         */
        fun repository(host: CodeHost, owner: String, repository: String) =
            Organization(name = repository, url = "${host.baseUrl}/$owner/$repository", host = host)
    }
}

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
    /** Where this position must land in the PDF. */
    val pageFit: PageFit = PageFit.Anywhere,
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
    override val pageFit: PageFit = PageFit.Anywhere,
) : Section

/** One row of the skills table: a category and the skills belonging to it. */
data class SkillEntry(
    val category: String,
    val skills: List<String>,
    /** Render targets this skills row appears in. */
    val scope: RenderScope = RenderScope.all,
    /** Where this skills row must land in the PDF. */
    val pageFit: PageFit = PageFit.Anywhere,
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
    override val pageFit: PageFit = PageFit.Anywhere,
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
    /** Where this project must land in the PDF. */
    val pageFit: PageFit = PageFit.Anywhere,
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
    override val pageFit: PageFit = PageFit.Anywhere,
) : Section

/** One education milestone: a year range and its rich-text description. */
data class EducationEntry(
    val years: String,
    val description: RichText,
    /** Render targets this milestone appears in. */
    val scope: RenderScope = RenderScope.all,
    /** Where this milestone must land in the PDF. */
    val pageFit: PageFit = PageFit.Anywhere,
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
    override val pageFit: PageFit = PageFit.Anywhere,
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
    /** Where this reference must land in the PDF. */
    val pageFit: PageFit = PageFit.Anywhere,
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
    override val pageFit: PageFit = PageFit.Anywhere,
) : Section
