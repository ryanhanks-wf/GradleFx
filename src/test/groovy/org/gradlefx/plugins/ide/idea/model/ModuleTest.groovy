/*
 * Copyright 2010 the original author or authors.
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
package org.gradlefx.plugins.ide.idea.model

import org.gradle.api.JavaVersion
import org.gradle.api.internal.xml.XmlTransformer
import org.gradle.plugins.ide.idea.model.JarDirectory
import org.gradle.plugins.ide.idea.model.ModuleDependency
import org.gradle.plugins.ide.idea.model.Path
import spock.lang.Specification

/**
 * @author Hans Dockter
 */
class ModuleTest extends Specification {
    final PathFactory pathFactory = new PathFactory()
    final XmlTransformer xmlTransformer = new XmlTransformer()
    final customSourceFolders = [path('file://$MODULE_DIR$/src/main/actionscript')] as LinkedHashSet
    final customTestSourceFolders = [path('file://$MODULE_DIR$/src/test/actionscript')] as LinkedHashSet
    final customExcludeFolders = [path('file://$MODULE_DIR$/build'),path('file://$MODULE_DIR$/.gradle')] as LinkedHashSet
    final customDependencies = [
            new ModuleLibrary(
                    [path('jar://$MODULE_DIR$/lib/flexunit-4.1.0-8-flex_4.1.0.16076.swc!/')] as Set<Path>,
                    [] as Set<Path>,
                    [] as Set<Path>,
                    [new JarDirectory(path('file://$MODULE_DIR$/lib'), false)] as Set<JarDirectory>)
    ]

    Module module = new Module(xmlTransformer, pathFactory)

    def loadFromReader() {
        when:
        module.load(customModuleReader)

        then:
        module.jdkName == "4.5.1.21328"
        module.sourceFolders == customSourceFolders
        module.testSourceFolders == customTestSourceFolders
        module.excludeFolders == customExcludeFolders
//        module.outputDir == path('file://$MODULE_DIR$/out')  // this is only on the build config
//        module.testOutputDir == path('file://$MODULE_DIR$/outTest') // this doesn't exist
        (module.dependencies as List) == customDependencies
    }

    def configureOverwritesDependenciesAndAppendsAllOtherEntries() {
        def constructorSourceFolders = [path('a')] as Set
        def constructorTestSourceFolders = [path('b')] as Set
        def constructorExcludeFolders = [path('c')] as Set
        def constructorInheritOutputDirs = false
        def constructorOutputDir = path('someOut')
        def constructorJavaVersion = JavaVersion.VERSION_1_6.toString()
        def constructorTestOutputDir = path('someTestOut')
        def constructorModuleDependencies = [
                customDependencies[0],
                new ModuleLibrary([path('x')], [], [], [new JarDirectory(path('y'), false)])] as LinkedHashSet

        when:
        module.load(customModuleReader)
        module.configure(null, constructorSourceFolders, constructorTestSourceFolders, constructorExcludeFolders,
                constructorInheritOutputDirs, constructorOutputDir, constructorTestOutputDir, constructorModuleDependencies, constructorJavaVersion)

        then:
        module.sourceFolders == customSourceFolders + constructorSourceFolders
        module.testSourceFolders == customTestSourceFolders + constructorTestSourceFolders
        module.excludeFolders == customExcludeFolders + constructorExcludeFolders
        module.outputDir == constructorOutputDir
        module.testOutputDir == constructorTestOutputDir
        module.jdkName == constructorJavaVersion.toString()
        module.dependencies == constructorModuleDependencies
    }

    def "configures default java version"() {
        when:
        module.configure(null, [] as Set, [] as Set, [] as Set,
                true, null, null, [] as Set, null)

        then:
        module.jdkName == Module.INHERITED
    }

    def loadDefaults() {
        when:
        module.loadDefaults()

        then:
        module.jdkName == Module.INHERITED
        module.sourceFolders == [] as Set
        module.dependencies.size() == 0
    }

    def generatedXmlShouldContainCustomValues() {
        def constructorSourceFolders = [new Path('a')] as Set
        def constructorOutputDir = new Path('someOut')
        def constructorTestOutputDir = new Path('someTestOut')

        when:
        module.loadDefaults()
        module.configure(null, constructorSourceFolders, [] as Set, [] as Set, false, constructorOutputDir, constructorTestOutputDir, [] as Set, null)
        def xml = toXmlReader
        def newModule = new Module(xmlTransformer, pathFactory)
        newModule.load(xml)

        then:
        this.module == newModule
    }

    private InputStream getToXmlReader() {
        ByteArrayOutputStream toXmlText = new ByteArrayOutputStream()
        module.store(toXmlText)
        return new ByteArrayInputStream(toXmlText.toByteArray())
    }

    private InputStream getCustomModuleReader() {
        return getClass().getResourceAsStream('customModule.xml')
    }

    private Path path(String url) {
        pathFactory.path(url)
    }
}