package org.gradlefx.plugins.ide.idea.model

import org.gradle.api.internal.xml.XmlTransformer
import org.gradle.internal.UncheckedException
import org.gradle.plugins.ide.idea.model.Dependency
import org.gradle.plugins.ide.idea.model.JarDirectory
import org.gradle.plugins.ide.idea.model.ModuleDependency
import org.gradle.plugins.ide.idea.model.Path
import org.gradle.plugins.ide.internal.generator.XmlPersistableConfigurationObject
import org.gradle.util.DeprecationLogger
import org.gradlefx.conventions.FlexType

/**
 * Created with IntelliJ IDEA.
 * User: ryanhanks
 * Date: 1/10/13
 * Time: 1:25 AM
 * To change this template use File | Settings | File Templates.
 */
class Module extends org.gradle.plugins.ide.idea.model.Module {
//    static final String INHERITED = "inherited"




//    /**
//     * The directory for the content root of the module.  Defaults to the project dirctory.
//     * If null, the directory containing the output file will be used.
//     */
//    Path contentPath
//
//    /**
//     * The directories containing the production sources. Must not be null.
//     */
//    Set<Path> sourceFolders = [] as LinkedHashSet
//
//    /**
//     * The directories containing the test sources. Must not be null.
//     */
//    Set<Path> testSourceFolders = [] as LinkedHashSet
//
//    /**
//     * The directories to be excluded. Must not be null.
//     */
//    Set<Path> excludeFolders = [] as LinkedHashSet
//
//    /**
//     * The dependencies of this module. Must not be null.
//     */
//    Set<Dependency> dependencies = [] as LinkedHashSet


    Set<BuildConfiguration> buildConfigurations = [] as LinkedHashSet

//    String jdkName
//
//    private final PathFactory pathFactory
//
    Module(XmlTransformer withXmlActions, PathFactory pathFactory) {
        super(withXmlActions, pathFactory)
    }

    @Override
    public void loadDefaults() {
        InputStream xml = new ByteArrayInputStream(('<?xml version="1.0" encoding="UTF-8"?>\n' +
                '<module relativePaths="true" type="Flex" version="4">\n' +
                '  <component name="FlexBuildConfigurationManager">\n' +
                '    <configurations />\n' +
                '    <compiler-options />\n' +
                '  </component>' +
                '    <component name="NewModuleRootManager" inherit-compiler-output="true">\n' +
                '        <exclude-output/>\n' +
                '        <orderEntry type="inheritedJdk"/>\n' +
                '        <content url="file://$MODULE_DIR$">\n' +
                '        </content>\n' +
                '        <orderEntry type="sourceFolder" forTests="false"/>\n' +
                '    </component>\n' +
                '    <component name="ModuleRootManager"/>\n' +
                '</module>').getBytes());

        try {
//            String defaultResourceName = getDefaultResourceName();
            InputStream inputStream = xml; //getClass().getClassLoader().getResourceAsStream(defaultResourceName);
            if (inputStream == null) {
                throw new IllegalStateException(String.format("Failed to load default resource '%s' of persistable configuration object of type '%s' (resource not found)", defaultResourceName, getClass().getName()));
            }
            try {
                load(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    @Override protected String getDefaultResourceName() {
        return 'flexModule.xml'
    }

    @Override protected void load(Node xml) {
        readJdkFromXml()
        readSourceAndExcludeFolderFromXml()
        readDependenciesFromXml()
        readBuildConfigurationsFromXml()
    }

//    private readJdkFromXml() {
//        def jdk = findOrderEntries().find { it.@type == 'jdk' }
//        jdkName = jdk ? jdk.@jdkName : INHERITED
//    }
//
    protected readSourceAndExcludeFolderFromXml() {
        findSourceFolder().each { sourceFolder ->
            if (sourceFolder.@isTestSource == 'false') {
                sourceFolders.add(pathFactory.path(sourceFolder.@url))
            } else {
                testSourceFolders.add(pathFactory.path(sourceFolder.@url))
            }
        }
        findExcludeFolder().each { excludeFolder ->
            excludeFolders.add(pathFactory.path(excludeFolder.@url))
        }
    }

    protected readDependenciesFromXml() {
        return findOrderEntries().each { orderEntry ->
            switch (orderEntry.@type) {
                case "module-library":
                    Set swcs = orderEntry.library.CLASSES.root.grep{ it.'@url'.startsWith('jar')}.collect {
                        pathFactory.path(it.@url)
                    }
                    Set asdoc = orderEntry.library.JAVADOC.root.collect {
                        pathFactory.path(it.@url)
                    }
                    Set sources = orderEntry.library.SOURCES.root.collect {
                        pathFactory.path(it.@url)
                    }
                    Set swcDirectories = orderEntry.library.jarDirectory.collect { new JarDirectory(pathFactory.path(it.@url), Boolean.parseBoolean(it.@recursive)) }
                    def moduleLibrary = new ModuleLibrary(swcs, asdoc, sources, swcDirectories)
                    dependencies.add(moduleLibrary)
                    break
                case "module":
                    dependencies.add(new ModuleDependency(orderEntry.@'module-name', orderEntry.@scope))
            }
        }
    }

    protected readBuildConfigurationsFromXml() {
        findBuildConfigurations().each { configuration ->
            println configuration
            String name = configuration.@name
            FlexType flexType
            switch (configuration.'@output-type'){
                case "Library":
                    flexType = FlexType.swc
                    break
            }
            Path outputFile = pathFactory.path(configuration.'@output-file')
            Path outputDir = pathFactory.path(configuration.'@output-folder')
            Node dependencies = configuration.dependencies[0]
            String targetPlayer = dependencies.'@target-player'
            String flexSdk = dependencies.sdk[0].@name
            BuildConfiguration buildConfiguration = new BuildConfiguration(
                    name,
                    flexType,
                    outputFile,
                    outputDir,
                    flexSdk,
                    targetPlayer,
                    [] as LinkedHashSet
            )
            buildConfigurations.add(buildConfiguration)

        }
    }

    protected findBuildConfigurations() {
        findFlexBuildConfigurationManager().configurations.configuration
    }

    protected Node findFlexBuildConfigurationManager(){
        xml.component.find { it.@name == 'FlexBuildConfigurationManager'}
    }


    protected def configure(Path contentPath, Set sourceFolders, Set testSourceFolders, Set excludeFolders,
                            Set dependencies, String jdkName) {
        this.contentPath = contentPath
        this.sourceFolders.addAll(sourceFolders)
        this.testSourceFolders.addAll(testSourceFolders)
        this.excludeFolders.addAll(excludeFolders)
        this.dependencies = dependencies; // overwrite rather than append dependencies
        if (jdkName) {
            this.jdkName = jdkName
        } else {
            this.jdkName =Module.INHERITED
        }
    }

    @Override protected void store(Node xml) {
        addJdkToXml()
        setContentURL()
        removeSourceAndExcludeFolderFromXml()
        addSourceAndExcludeFolderToXml()

        removeDependenciesFromXml()
        addDependenciesToXml()
    }

    protected addJdkToXml() {
        assert jdkName != null
        Node moduleJdk = findOrderEntries().find { it.@type == 'jdk' }
        if (jdkName != INHERITED) {
            Node inheritedJdk = findOrderEntries().find { it.@type == "inheritedJdk" }
            if (inheritedJdk) {
                inheritedJdk.parent().remove(inheritedJdk)
            }
            if (moduleJdk) {
                findNewModuleRootManager().remove(moduleJdk)
            }
            findNewModuleRootManager().appendNode("orderEntry", [type: "jdk", jdkName: jdkName, jdkType: "JavaSDK"])
        } else if (!(findOrderEntries().find { it.@type == "inheritedJdk" })) {
            if (moduleJdk) {
                findNewModuleRootManager().remove(moduleJdk)
            }
            findNewModuleRootManager().appendNode("orderEntry", [type: "inheritedJdk"])
        }
    }

    protected setContentURL() {
        if (contentPath != null) {
            findContent().@url = contentPath.url
        }
    }

    protected Set addDependenciesToXml() {
        return dependencies.each { Dependency dependency ->
            dependency.addToNode(findNewModuleRootManager())
        }
    }

    protected addSourceAndExcludeFolderToXml() {
        sourceFolders.each { Path path ->
            findContent().appendNode('sourceFolder', [url: path.url, isTestSource: 'false'])
        }
        testSourceFolders.each { Path path ->
            findContent().appendNode('sourceFolder', [url: path.url, isTestSource: 'true'])
        }
        excludeFolders.each { Path path ->
            findContent().appendNode('excludeFolder', [url: path.url])
        }
    }

    protected removeSourceAndExcludeFolderFromXml() {
        findSourceFolder().each { sourceFolder ->
            findContent().remove(sourceFolder)
        }
        findExcludeFolder().each { excludeFolder ->
            findContent().remove(excludeFolder)
        }
    }

    protected removeDependenciesFromXml() {
        return findOrderEntries().each { orderEntry ->
            if (isDependencyOrderEntry(orderEntry)) {
                findNewModuleRootManager().remove(orderEntry)
            }
        }
    }

    protected boolean isDependencyOrderEntry(def orderEntry) {
        ['module-library', 'module'].contains(orderEntry.@type)
    }

    protected Node findContent() {
        findNewModuleRootManager().content[0]
    }

    protected findSourceFolder() {
        findContent().sourceFolder
    }

    protected findExcludeFolder() {
        findContent().excludeFolder
    }

    protected Node findNewModuleRootManager() {
        xml.component.find { it.@name == 'NewModuleRootManager'}
    }

    protected findOrderEntries() {
        findNewModuleRootManager().orderEntry
    }


    boolean equals(o) {
        if (this.is(o)) { return true }

        if (getClass() != o.class) { return false }

        Module module = (Module) o

        if (dependencies != module.dependencies) { return false }
        if (excludeFolders != module.excludeFolders) { return false }
        if (sourceFolders != module.sourceFolders) { return false }
        if (testSourceFolders != module.testSourceFolders) { return false }

        return true
    }

    int hashCode() {
        int result;

        result = (sourceFolders != null ? sourceFolders.hashCode() : 0)
        result = 31 * result + (testSourceFolders != null ? testSourceFolders.hashCode() : 0)
        result = 31 * result + (excludeFolders != null ? excludeFolders.hashCode() : 0)
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0)
        return result
    }


    String toString() {
        return "Module{" +
                "dependencies=" + dependencies +
                ", sourceFolders=" + sourceFolders +
                ", testSourceFolders=" + testSourceFolders +
                ", excludeFolders=" + excludeFolders +
                '}'
    }
}
