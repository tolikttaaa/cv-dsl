package example

import cv.dsl.cv
import cv.generation.CvApplication
import cv.model.Organization
import cv.model.RenderScope
import cv.model.RenderTarget

private val simpleCv = cv {
    firstName = "Ada"
    lastName = "Lovelace"
    tagline = "Computing pioneer"
    footerText = "Ada Lovelace — CV"

    social {
        row {
            email("ada@example.com")
            github("ada")
            // The address appears in the PDF only — scopes hide elements per render target.
            address("London, UK", scope = RenderScope.only(RenderTarget.PDF))
        }
    }

    summary("Summary", "faUser", webTitle = "About") {
        paragraph("I turn mathematical ideas into practical algorithms.") {
            highlight("algorithms", bold, colored(cv.model.CvColor.ACCENT))
        }
    }

    experience("Experience", "faSuitcase", id = "experience") {
        work(
            role = "Mathematician",
            company = Organization("Analytical Engines", "https://en.wikipedia.org/wiki/Analytical_engine"),
            location = "London",
            dates = "1842 – 1843",
            tags = listOf("Algorithms", "Mathematics", "Research"),
        ) {
            paragraph("Translated and expanded the first published description of the Analytical Engine.") {
                bold("expanded")
            }
            bullets {
                item("Published the first algorithm designed for a machine.") { bold("first algorithm") }
                item("Explained how machines could manipulate symbols beyond arithmetic.")
            }
        }
    }

    skills("Skills", "faCode") {
        entry("Technical", listOf("Algorithms", "Mathematics", "Technical writing"))
        entry("Collaboration", listOf("Research", "Peer review"))
    }

    education("Education", "faGraduationCap") {
        entry(
            years = "1828 – 1835",
            degree = "Private study in advanced mathematics",
            institution = Organization("University of London", "https://www.london.ac.uk"),
            location = "London",
        )
    }
}

fun main(args: Array<String>) = CvApplication(simpleCv).run(args)
