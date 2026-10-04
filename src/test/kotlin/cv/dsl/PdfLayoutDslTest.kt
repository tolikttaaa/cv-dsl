package cv.dsl

import cv.model.EducationSection
import cv.model.Organization
import cv.model.PageFit
import cv.model.PdfLayout
import cv.model.ProjectsSection
import cv.model.ReferencesSection
import cv.model.SkillsSection
import cv.model.SummarySection
import cv.model.WorksSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PdfLayoutDslTest {
    @Test
    fun `pdf block sets the print settings and defaults to the class defaults`() {
        assertEquals(PdfLayout(), cv {}.pdf)
        assertEquals(PdfLayout(fontSize = 9.5, maxPages = 2), cv { pdf { fontSize = 9.5; maxPages = 2 } }.pdf)
        assertEquals(PdfLayout(maxPages = 1), cv { pdf { maxPages = 1 } }.pdf)
    }

    @Test
    fun `rejects unusable print settings and page numbers`() {
        val small = assertFailsWith<IllegalArgumentException> { cv { pdf { fontSize = 5.5 } } }
        assertTrue(small.message.orEmpty().contains("between 6.0 and 16.0 pt"))
        assertFailsWith<IllegalArgumentException> { PdfLayout(fontSize = 16.5) }
        val pages = assertFailsWith<IllegalArgumentException> { cv { pdf { maxPages = 0 } } }
        assertTrue(pages.message.orEmpty().contains("at least 1"))
        val page = assertFailsWith<IllegalArgumentException> { PageFit.OnPage(0) }
        assertTrue(page.message.orEmpty().contains("start at 1"))
    }

    @Test
    fun `every section and entry carries its page rule`() {
        val organization = Organization("Org")
        val built = cv {
            summary("Summary", "faUser", pageFit = PageFit.OnPage(1)) { paragraph("text") }
            experience("Experience", "faSuitcase", id = "experience", pageFit = PageFit.SinglePage) {
                work("Role", organization, "City", "2020", emptyList(), pageFit = PageFit.OnPage(1)) {
                    paragraph("text")
                }
                work("Other", organization, "City", "2021", emptyList()) { paragraph("text") }
            }
            skills("Skills", "faCode", pageFit = PageFit.OnPage(2)) {
                entry("Languages", listOf("Kotlin"), pageFit = PageFit.SinglePage)
            }
            projects("Projects", "faLaptop", pageFit = PageFit.OnPage(2)) {
                project("Tool", organization, "2022", emptyList(), pageFit = PageFit.OnPage(2)) {
                    paragraph("text")
                }
            }
            education("Education", "faUserGraduate", pageFit = PageFit.OnPage(2)) {
                entry("2018", pageFit = PageFit.OnPage(2)) { +"School" }
                entry("2022", "Degree", organization, "City", pageFit = PageFit.SinglePage)
            }
            references("References", "faQuoteLeft", pageFit = PageFit.SinglePage) {
                referee("Name", "Role", organization, "2020", "a@example.com", pageFit = PageFit.OnPage(2))
            }
        }

        val summary = assertIs<SummarySection>(built.sections[0])
        assertEquals(PageFit.OnPage(1), summary.pageFit)
        val works = assertIs<WorksSection>(built.sections[1])
        assertEquals(PageFit.SinglePage, works.pageFit)
        assertEquals(listOf(PageFit.OnPage(1), PageFit.Anywhere), works.works.map { it.pageFit })
        val skills = assertIs<SkillsSection>(built.sections[2])
        assertEquals(PageFit.SinglePage, skills.entries.single().pageFit)
        val projects = assertIs<ProjectsSection>(built.sections[3])
        assertEquals(PageFit.OnPage(2), projects.projects.single().pageFit)
        val education = assertIs<EducationSection>(built.sections[4])
        assertEquals(listOf(PageFit.OnPage(2), PageFit.SinglePage), education.entries.map { it.pageFit })
        val references = assertIs<ReferencesSection>(built.sections[5])
        assertEquals(PageFit.SinglePage, references.pageFit)
        assertEquals(PageFit.OnPage(2), references.referees.single().pageFit)
    }
}
