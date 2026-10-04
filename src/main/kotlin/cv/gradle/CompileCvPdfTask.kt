package cv.gradle

import cv.layout.LayoutManifest
import cv.layout.PageMarks
import cv.layout.PdfLayoutReport
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Compiles generated LaTeX sources twice so references and page data settle,
 * then verifies the PDF layout rules (page limit, per-element page rules)
 * against the pages LuaLaTeX recorded. Any violated rule fails the task; the
 * compiled PDF is still written so the layout can be inspected.
 */
@DisableCachingByDefault(because = "Output depends on the locally installed LuaLaTeX distribution")
abstract class CompileCvPdfTask : DefaultTask() {
    @get:Input
    abstract val lualatexExecutable: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val latexDirectory: DirectoryProperty

    @get:OutputFile
    abstract val pdfFile: RegularFileProperty

    @get:OutputFile
    abstract val logFile: RegularFileProperty

    /** Where every tracked element landed in the PDF; not written when unset. */
    @get:Optional
    @get:OutputFile
    abstract val layoutReportFile: RegularFileProperty

    @TaskAction
    fun compile() {
        val latexDir = latexDirectory.get().asFile
        val mainFile = latexDir.resolve("cv.tex")
        if (!mainFile.isFile) {
            throw GradleException("Generated LaTeX entry point is missing: $mainFile")
        }

        val outputDir = pdfFile.get().asFile.parentFile.apply { mkdirs() }
        val log = logFile.get().asFile
        repeat(PASSES) { pass ->
            val exitCode = ProcessBuilder(
                lualatexExecutable.get(),
                "-interaction=nonstopmode",
                "-output-directory=${outputDir.absolutePath}",
                mainFile.name,
            )
                .directory(latexDir)
                .redirectOutput(log)
                .redirectErrorStream(true)
                .start()
                .waitFor()
            if (exitCode != 0) {
                throw GradleException(
                    "LuaLaTeX pass ${pass + 1} failed with exit code $exitCode. See $log",
                )
            }
        }
        logger.lifecycle("Compiled ${pdfFile.get().asFile}")
        verifyLayout(latexDir, outputDir.resolve("${mainFile.nameWithoutExtension}.${PageMarks.EXTENSION}"))
    }

    private fun verifyLayout(latexDir: File, marksFile: File) {
        val manifestFile = latexDir.resolve(LayoutManifest.FILE_NAME)
        if (!manifestFile.isFile) {
            logger.info("No PDF layout manifest in $latexDir; skipping layout verification.")
            return
        }
        val manifest = LayoutManifest.parse(manifestFile.readText())
        if (!marksFile.isFile) {
            if (manifest.hasRules) {
                throw GradleException(
                    "LuaLaTeX did not record page marks in $marksFile, so the PDF layout rules cannot be verified. " +
                        "Regenerate the LaTeX sources with this cv-dsl version.",
                )
            }
            return
        }
        val report = PdfLayoutReport.of(manifest, PageMarks.parse(marksFile.readText()))
        val reportFile = layoutReportFile.orNull?.asFile
        reportFile?.apply { parentFile.mkdirs() }?.writeText(report.render())
        val violations = report.violations
        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("${report.summary}:")
                    violations.forEach { appendLine("  - $it") }
                    append("Shorten the content or lower fontSize in the CV's pdf { } block")
                    append(reportFile?.let { "; every element's pages are listed in $it." } ?: ".")
                },
            )
        }
        logger.lifecycle(report.summary)
    }

    private companion object {
        const val PASSES = 2
    }
}
