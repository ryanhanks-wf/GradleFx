package org.gradlefx.util

import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder

/**
 * Created with IntelliJ IDEA.
 * User: ryanhanks
 * Date: 1/10/13
 * Time: 9:34 PM
 * To change this template use File | Settings | File Templates.
 */
class HelperUtil {
    static DefaultProject createRootProject() {
        createRootProject(TemporaryFolder.newInstance().dir)
    }

    static DefaultProject createRootProject(File rootDir) {
        return ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
    }

    static DefaultProject createChildProject(DefaultProject parent, String name, File projectDir = null) {
        return ProjectBuilder
                .builder()
                .withName(name)
                .withParent(parent)
                .withProjectDir(projectDir)
                .build();
    }
}
