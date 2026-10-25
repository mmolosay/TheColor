package io.github.mmolosay.thecolor.buildlogic

import org.gradle.api.Project

object GitUtils {

    val Project.headCommitShortHash: String?
        get() =
            runCatching {
                providers
                    .exec { commandLine("git", "rev-parse", "--short", "HEAD") }
                    .standardOutput.asText.get()
                    .trim()
            }.getOrNull()
}