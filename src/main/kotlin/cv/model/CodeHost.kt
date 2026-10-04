package cv.model

/**
 * A code-hosting platform an [Organization] lives on. Renderers show it after
 * the organization name with the platform's icon, e.g. `cv-dsl | [GitHub icon] GitHub`.
 *
 * @property displayName Platform name shown next to its icon.
 * @property baseUrl Web root that repository URLs are built from.
 */
enum class CodeHost(val displayName: String, val baseUrl: String) {
    GITHUB("GitHub", "https://github.com"),
    GITLAB("GitLab", "https://gitlab.com"),
    BITBUCKET("Bitbucket", "https://bitbucket.org"),
}
