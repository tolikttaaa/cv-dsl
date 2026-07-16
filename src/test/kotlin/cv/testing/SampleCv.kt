package cv.testing

import cv.dsl.cv
import cv.model.Cv
import cv.model.CvColor
import cv.model.Organization

internal val sampleCv: Cv = cv {
    firstName = "Ada"
    lastName = "Lovelace"
    tagline = "Computing pioneer & mathematician"
    footerText = "Ada Lovelace — CV"
    hyphenation = false
    photo("portrait.png")

    social {
        row {
            email("ada@example.com")
            github("ada")
            linkedin("ada-lovelace")
        }
        row {
            phone("+44 20 0000 0000")
            telegram("ada")
            leetcode("ada")
            address("London, UK")
        }
    }

    summary("Summary", "faUser", webTitle = "About") {
        paragraph("I turn algorithms into practical machines.") {
            highlight("algorithms", linkTo("https://example.com/algorithms"), bold)
            italic("practical")
            colored(CvColor.ACCENT, "machines")
        }
        bullets {
            item("First published algorithm") { nowrap("published algorithm") }
            item("Analytical Engine collaborator")
        }
    }

    experience("Experience", "faSuitcase", "experience") {
        work(
            role = "Mathematician",
            company = Organization("Analytical Engines", "https://example.com/engine"),
            location = "London",
            dates = "1842 – 1843",
            tags = listOf("Algorithms", "Mathematics"),
        ) {
            paragraph("Translated and expanded Menabrea's article.") { bold("expanded") }
        }
    }

    skills("Skills", "faCode") {
        entry("Technical", listOf("Algorithms", "Bernoulli numbers"))
    }

    projects("Projects", "faLaptop") {
        project(
            name = "Note G",
            company = Organization("Independent"),
            dates = "1843",
            tags = listOf("Research"),
        ) {
            paragraph("A method for calculating Bernoulli numbers.")
        }
    }

    education("Education", "faGraduationCap") {
        entry(
            years = "1828 – 1835",
            degree = "Private study",
            institution = Organization("University of London", "https://www.london.ac.uk"),
            location = "London",
        )
        entry("1835") { +"Advanced mathematics with Augustus De Morgan" }
    }

    references("References", "faQuoteLeft") {
        referee(
            name = "Charles Babbage",
            role = "Inventor",
            company = Organization("Analytical Engine", "https://example.com"),
            period = "1833 – 1852",
            email = "charles@example.com",
        )
    }
}
