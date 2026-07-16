package cv.dsl

import cv.model.Bold
import cv.model.Bullets
import cv.model.Colored
import cv.model.CvColor
import cv.model.EducationSection
import cv.model.Italic
import cv.model.Link
import cv.model.NoWrap
import cv.model.Organization
import cv.model.Paragraph
import cv.model.Photo
import cv.model.Plain
import cv.model.ProjectsSection
import cv.model.ReferencesSection
import cv.model.SkillsSection
import cv.model.Social
import cv.model.SummarySection
import cv.model.WorksSection
import cv.testing.sampleCv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CvDslTest {
    @Test
    fun `builds a complete immutable CV in declaration order`() {
        assertEquals("Ada", sampleCv.firstName)
        assertEquals("Lovelace", sampleCv.lastName)
        assertEquals(Photo("portrait.png", "2.2cm"), sampleCv.photo)
        assertFalse(sampleCv.hyphenation)
        assertEquals(
            listOf(
                SummarySection::class,
                WorksSection::class,
                SkillsSection::class,
                ProjectsSection::class,
                EducationSection::class,
                ReferencesSection::class,
            ),
            sampleCv.sections.map { it::class },
        )
        assertEquals("About", sampleCv.sections.first().webTitle)
        assertEquals(
            listOf(
                Social.Email("ada@example.com"),
                Social.GitHub("ada"),
                Social.LinkedIn("ada-lovelace"),
            ),
            sampleCv.social.first(),
        )
        assertEquals(7, sampleCv.social.flatten().size)
    }

    @Test
    fun `normalizes multiline paragraphs and applies nested highlight styles`() {
        val description = DescriptionBuilder().apply {
            paragraph(
                """
                    Kotlin   makes
                    DSLs readable. Kotlin wins.
                """,
            ) {
                highlight("Kotlin", linkTo("https://kotlinlang.org"), bold)
                italic(Regex("DSLs"))
                colored(CvColor.ACCENT, "readable")
                nowrap("wins")
            }
        }.build()

        val paragraph = assertIs<Paragraph>(description.blocks.single())
        val nodes = paragraph.text.inlines
        assertIs<Link>(nodes[0])
        assertIs<Bold>((nodes[0] as Link).content.single())
        assertIs<Italic>(nodes[2])
        assertIs<Colored>(nodes[4])
        assertIs<Link>(nodes[6])
        assertIs<NoWrap>(nodes[8])
        assertEquals("Kotlin", (((nodes[6] as Link).content.single() as Bold).content.single() as Plain).text)
        assertEquals("wins", ((nodes[8] as NoWrap).content.single() as Plain).text)
    }

    @Test
    fun `earlier rules own overlapping text`() {
        val result = highlightedText("Kotlin DSL", block = {
            bold("Kotlin")
            italic("DSL")
        })

        assertEquals(
            listOf(Bold(listOf(Plain("Kotlin"))), Plain(" "), Italic(listOf(Plain("DSL")))),
            result.inlines,
        )
    }

    @Test
    fun `rejects invalid highlight rules`() {
        val noStyles = assertFailsWith<IllegalArgumentException> {
            highlightedText("text") { highlight("text") }
        }
        assertTrue(noStyles.message.orEmpty().contains("at least one style"))

        val noMatch = assertFailsWith<IllegalStateException> {
            highlightedText("text") { bold("missing") }
        }
        assertTrue(noMatch.message.orEmpty().contains("matched nothing"))

        val emptyRegex = assertFailsWith<IllegalStateException> {
            highlightedText("text") { bold(Regex("")) }
        }
        assertTrue(emptyRegex.message.orEmpty().contains("matched nothing"))
    }

    @Test
    fun `text DSL supports plain and every nested style`() {
        val text = richText {
            +"plain "
            bold { italic("important") }
            colored(CvColor.DARK_GREY, " muted")
            link("https://example.com", " link")
            nowrap(" fixed")
        }

        assertEquals(5, text.inlines.size)
        assertIs<Plain>(text.inlines[0])
        assertIs<Bold>(text.inlines[1])
        assertIs<Italic>((text.inlines[1] as Bold).content.single())
        assertIs<Colored>(text.inlines[2])
        assertIs<Link>(text.inlines[3])
        assertIs<NoWrap>(text.inlines[4])
        assertEquals(listOf(Plain("simple")), plainText("simple").inlines)
    }

    @Test
    fun `section builders preserve structured values`() {
        val summary = assertIs<SummarySection>(sampleCv.sections[0])
        assertIs<Bullets>(summary.text.blocks[1])

        val works = assertIs<WorksSection>(sampleCv.sections[1])
        assertEquals("Mathematician", works.works.single().role)
        assertEquals(listOf("Algorithms", "Mathematics"), works.works.single().tags)

        val skills = assertIs<SkillsSection>(sampleCv.sections[2])
        assertEquals("Technical", skills.entries.single().category)

        val projects = assertIs<ProjectsSection>(sampleCv.sections[3])
        assertEquals(Organization("Independent"), projects.projects.single().company)

        val education = assertIs<EducationSection>(sampleCv.sections[4])
        assertEquals(2, education.entries.size)
        assertTrue(education.entries.first().description.inlines.any { it is Link })

        val references = assertIs<ReferencesSection>(sampleCv.sections[5])
        assertEquals("charles@example.com", references.referees.single().email)
    }

    @Test
    fun `uses defaults for an empty CV`() {
        val empty = cv {}
        assertEquals("", empty.firstName)
        assertEquals("", empty.lastName)
        assertEquals("", empty.tagline)
        assertEquals("", empty.footerText)
        assertTrue(empty.hyphenation)
        assertEquals(null, empty.photo)
        assertTrue(empty.social.isEmpty())
        assertTrue(empty.sections.isEmpty())
    }
}
