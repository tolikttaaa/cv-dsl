package cv.render

import cv.model.Cv
import cv.model.EducationSection
import cv.model.ProjectsSection
import cv.model.ReferencesSection
import cv.model.RenderTarget
import cv.model.SkillsSection
import cv.model.SummarySection
import cv.model.WorksSection

/** Returns a CV filtered for [target]; elements are visible to every target by default. */
internal fun Cv.visibleTo(target: RenderTarget): Cv = copy(
    sections = sections
        .filter { it.scope.includes(target) }
        .map { section ->
            when (section) {
                is SummarySection -> section.copy()
                is WorksSection -> section.copy(works = section.works.filter { it.scope.includes(target) })
                is SkillsSection -> section.copy(entries = section.entries.filter { it.scope.includes(target) })
                is ProjectsSection -> section.copy(projects = section.projects.filter { it.scope.includes(target) })
                is EducationSection -> section.copy(entries = section.entries.filter { it.scope.includes(target) })
                is ReferencesSection -> section.copy(referees = section.referees.filter { it.scope.includes(target) })
            }
        },
    social = social
        .map { row -> row.filter { it.scope.includes(target) } }
        .filter { it.isNotEmpty() },
)
