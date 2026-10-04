package cv.render

import cv.model.CodeHost
import cv.model.Organization
import cv.render.latex.renderLatex
import cv.render.markdown.renderMarkdown
import cv.render.web.renderWeb
import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryOrganizationTest {
    private val engine = Organization.repository(CodeHost.GITHUB, "ada", "engine")

    @Test
    fun `repository factory names the repository and links to it on its host`() {
        assertEquals(Organization("engine", "https://github.com/ada/engine", CodeHost.GITHUB), engine)
        assertEquals(
            "https://gitlab.com/ada/notes",
            Organization.repository(CodeHost.GITLAB, "ada", "notes").url,
        )
        assertEquals(
            "https://bitbucket.org/ada/tables",
            Organization.repository(CodeHost.BITBUCKET, "ada", "tables").url,
        )
    }

    @Test
    fun `LaTeX appends the host name and icon in the same style inside the link`() {
        assertEquals(
            """\link{https://github.com/ada/engine}{\textbf{engine \textbar{} GitHub\enspace\githubSymbol}}""",
            engine.renderLatex(emphasized = true),
        )
        assertEquals(
            """notes \textbar{} GitLab\enspace\gitlabSymbol""",
            Organization("notes", host = CodeHost.GITLAB).renderLatex(emphasized = false),
        )
        assertEquals(
            """tables \textbar{} Bitbucket\enspace\bitbucketSymbol""",
            Organization("tables", host = CodeHost.BITBUCKET).renderLatex(emphasized = false),
        )
    }

    @Test
    fun `web appends the host name and brand icon in the same style inside the link`() {
        assertEquals(
            "<a href=\"https://github.com/ada/engine\" target=\"_blank\" rel=\"noopener\">" +
                "<strong>engine | GitHub <i class=\"fa-brands fa-github\"></i></strong></a>",
            engine.renderWeb(emphasized = true),
        )
        assertEquals(
            "notes | GitLab <i class=\"fa-brands fa-gitlab\"></i>",
            Organization("notes", host = CodeHost.GITLAB).renderWeb(emphasized = false),
        )
        assertEquals(
            "tables | Bitbucket <i class=\"fa-brands fa-bitbucket\"></i>",
            Organization("tables", host = CodeHost.BITBUCKET).renderWeb(emphasized = false),
        )
    }

    @Test
    fun `Markdown appends the host name inside the link`() {
        assertEquals("**[engine \\| GitHub](https://github.com/ada/engine)**", engine.renderMarkdown(emphasized = true))
        val notes = Organization("notes", host = CodeHost.GITLAB)
        assertEquals("notes \\| GitLab", notes.renderMarkdown(emphasized = false))
    }

    @Test
    fun `organizations without a host render as before`() {
        val plain = Organization("Azul", "https://www.azul.com/")
        assertEquals("""\link{https://www.azul.com/}{\textbf{Azul}}""", plain.renderLatex(emphasized = true))
        assertEquals(
            "<a href=\"https://www.azul.com/\" target=\"_blank\" rel=\"noopener\"><strong>Azul</strong></a>",
            plain.renderWeb(emphasized = true),
        )
        assertEquals("**[Azul](https://www.azul.com/)**", plain.renderMarkdown(emphasized = true))
    }
}
