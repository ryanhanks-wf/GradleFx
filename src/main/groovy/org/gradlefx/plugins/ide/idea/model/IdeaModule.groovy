package org.gradlefx.plugins.ide.idea.model

import org.gradle.plugins.ide.idea.model.IdeaModuleIml

/**
 * Created with IntelliJ IDEA.
 * User: ryanhanks
 * Date: 1/10/13
 * Time: 1:59 AM
 * To change this template use File | Settings | File Templates.
 */
class IdeaModule extends org.gradle.plugins.ide.idea.model.IdeaModule {
    IdeaModule(org.gradle.api.Project project, IdeaModuleIml iml) {
        super(project, iml)
    }

    @Override
    void mergeXmlModule(Module xmlModule) {
        iml.beforeMerged.execute(xmlModule)

        def path = { getPathFactory().path(it) }
        def contentRoot = path(getContentRoot())
        Set sourceFolders = getSourceDirs().findAll { it.exists() }.collect { path(it) }
        Set testSourceFolders = getTestSourceDirs().findAll { it.exists() }.collect { path(it) }
        Set excludeFolders = getExcludeDirs().collect { path(it) }
        def outputDir = getOutputDir() ? path(getOutputDir()) : null
        def testOutputDir = getTestOutputDir() ? path(getTestOutputDir()) : null
        Set dependencies = resolveDependencies()

        xmlModule.configure(contentRoot, sourceFolders, testSourceFolders, excludeFolders,
                dependencies, getJdkName())

        iml.whenMerged.execute(xmlModule)
    }
}

