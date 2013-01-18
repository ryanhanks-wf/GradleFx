/*
 * Copyright (c) 2011 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradlefx.plugins.ide.idea

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ide.api.XmlFileContentMerger
import org.gradle.plugins.ide.idea.GenerateIdeaProject
import org.gradle.plugins.ide.idea.GenerateIdeaWorkspace
import org.gradle.plugins.ide.idea.internal.IdeaNameDeduper
import org.gradle.plugins.ide.idea.model.*
import org.gradle.plugins.ide.internal.IdePlugin
import org.gradlefx.plugins.GradleFxPlugin
import org.gradlefx.plugins.ide.idea.model.IdeaModule
import org.gradlefx.plugins.ide.idea.model.PathFactory

import javax.inject.Inject

class IdeaPlugin extends org.gradle.plugins.ide.idea.IdeaPlugin {
    IdeaModel model

    @Inject IdeaPlugin(Instantiator instantiator){
        this.instantiator = instantiator
    }

    @Override protected String getLifecycleTaskName() {
        return 'idea'
    }


    @Override protected void onApply(Project project) {
        lifecycleTask.description = 'Generates IDEA project files (IML, IPR, IWS)'
        cleanTask.description = 'Cleans IDEA project files (IML, IPR)'

        model = project.extensions.create("idea", IdeaModel)

        configureIdeaWorkspace(project)
        configureIdeaProject(project)
        configureIdeaModule(project)
        configureForGradleFxPlugin(project)

        hookDeduplicationToTheRoot(project)

//        configureIdeaGradleFxModule(project)
//        configureForGradleFxPlu gin(project)
    }

    protected configureIdeaWorkspace(Project project) {
        if (isRoot(project)) {
            def task = project.task('ideaWorkspace', description: 'Generates an IDEA workspace file (IWS)', type: GenerateIdeaWorkspace) {
                workspace = new IdeaWorkspace(iws: new XmlFileContentMerger(xmlTransformer))
                model.workspace = workspace
                outputFile = new File(project.projectDir, project.name + ".iws")
            }
            addWorker(task, false)
        }
    }

    protected boolean isRoot(Project project) {
        return project.parent == null
    }

    protected configureIdeaProject(Project project) {
        if (isRoot(project)) {
            def task = project.task('ideaProject', description: 'Generates IDEA project file (IPR)', type: GenerateIdeaProject) {
                def ipr = new XmlFileContentMerger(xmlTransformer)
                ideaProject = instantiator.newInstance(IdeaProject, ipr)

                model.project = ideaProject

                ideaProject.outputFile = new File(project.projectDir, project.name + ".ipr")
                ideaProject.conventionMapping.jdkName = { JavaVersion.current().toString() }
                ideaProject.conventionMapping.languageLevel = { new IdeaLanguageLevel(JavaVersion.VERSION_1_6) }
                ideaProject.wildcards = ['!?*.java', '!?*.groovy'] as Set
                ideaProject.conventionMapping.modules = {
                    project.rootProject.allprojects.findAll { it.plugins.hasPlugin(IdeaPlugin) }.collect { it.idea.module }
                }

                ideaProject.conventionMapping.pathFactory = {
                    new PathFactory().addPathVariable('PROJECT_DIR', outputFile.parentFile)
                }
            }
            addWorker(task)
        }
    }

    protected configureIdeaModule(Project project) {
//        def task = project.task('ideaModule', description: 'Generates IDEA module files (IML)', type: GenerateIdeaModule) {
//            def iml = new IdeaModuleIml(xmlTransformer, project.projectDir)
//            module = instantiator.newInstance(IdeaModule, project, iml)
//
//            model.module = module
//
//            module.conventionMapping.sourceDirs = { [] as LinkedHashSet }
//            module.conventionMapping.name = { project.name }
//            module.conventionMapping.contentRoot = { project.projectDir }
//            module.conventionMapping.testSourceDirs = { [] as LinkedHashSet }
//            module.conventionMapping.excludeDirs = { [project.buildDir, project.file('.gradle')] as LinkedHashSet }
//
//            module.conventionMapping.pathFactory = {
//                PathFactory factory = new PathFactory()
//                factory.addPathVariable('MODULE_DIR', outputFile.parentFile)
//                module.pathVariables.each { key, value ->
//                    factory.addPathVariable(key, value)
//                }
//                factory
//            }
//        }
//
//        addWorker(task)
    }


    protected configureForGradleFxPlugin(Project project) {
        project.plugins.withType(GradleFxPlugin) {
            configureIdeaProjectForGradleFx(project)
            configureIdeaModuleForGradleFx(project)
        }
    }

    protected configureIdeaProjectForGradleFx(Project project) {
        if (isRoot(project)) {
//            project.idea.project.conventionMapping.languageLevel = {
//                new IdeaLanguageLevel(project.sourceCompatibility)
//            }
        }
    }

    private configureIdeaModuleForGradleFx(Project project) {
        project.ideaModule {
            module.conventionMapping.sourceDirs = { project.files(project.srcDirs).getFiles() }
            module.conventionMapping.testSourceDirs = { project.files(project.testDirs).files }
            def configurations = project.configurations
//            module.scopes = [:]
//            module.conventionMapping.singleEntryLibraries = {
//                [:]
//            }
//            dependsOn {
//                [:]
////                project.sourceSets.main.output.dirs + project.sourceSets.test.output.dirs
//            }
        }
    }



    protected configureIdeaGradleFxModule(Project project) {
        def task = project.task('ideaGradleFxModule', description: 'Generates IDEA module files (IML)', type: org.gradlefx.plugins.ide.idea.GenerateIdeaModule) {
//        def task = project.task('ideaModule', description: 'Generates IDEA module files (IML)', type: GenerateIdeaModule) {
            def iml = new IdeaModuleIml(xmlTransformer, project.projectDir)
            module = instantiator.newInstance(IdeaModule, project, iml)

            model.module = module

            module.conventionMapping.sourceDirs = { [] as LinkedHashSet }
            module.conventionMapping.name = { project.name }
            module.conventionMapping.contentRoot = { project.projectDir }
            module.conventionMapping.testSourceDirs = { [] as LinkedHashSet }
            module.conventionMapping.excludeDirs = { [project.buildDir, project.file('.gradle')] as LinkedHashSet }

            module.conventionMapping.pathFactory = {
                PathFactory factory = new PathFactory()
                factory.addPathVariable('MODULE_DIR', outputFile.parentFile)
                module.pathVariables.each { key, value ->
                    factory.addPathVariable(key, value)
                }
                factory
            }
        }

        addWorker(task)
    }




    void hookDeduplicationToTheRoot(Project project) {
        if (isRoot(project)) {
            project.gradle.projectsEvaluated {
                makeSureModuleNamesAreUnique()
            }
        }
    }

    public void makeSureModuleNamesAreUnique() {
        new IdeaNameDeduper().configureRoot(project.rootProject)
    }


//    private configureIdeaModuleForGradleFx(Project project) {
//        println project.flexHome
//        println project.srcDirs
//        println project.getDependencyProjects()
//        project.ideaModule {
////            module.conventionMapping.sourceDirs = { project.srcDirs }
////            module.conventionMapping.testSourceDirs = { project.sourceSets.test.allSource.srcDirs as LinkedHashSet }
////            def configurations = project.configurations
////            module.scopes = [
////                    PROVIDED: [plus: [], minus: []],
////                    COMPILE: [plus: [configurations.compile], minus: []],
////                    RUNTIME: [plus: [configurations.runtime], minus: [configurations.compile]],
////                    TEST: [plus: [configurations.testRuntime], minus: [configurations.runtime]]
////            ]
////            module.conventionMapping.singleEntryLibraries = {
////                [
////                        RUNTIME: project.sourceSets.main.output.dirs,
////                        TEST: project.sourceSets.test.output.dirs
////                ]
////            }
////            dependsOn {
////                project.sourceSets.main.output.dirs + project.sourceSets.test.output.dirs
////            }
//        }
//    }
}

