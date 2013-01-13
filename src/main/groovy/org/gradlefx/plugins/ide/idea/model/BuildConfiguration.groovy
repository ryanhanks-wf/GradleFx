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

import org.gradle.api.internal.xml.XmlTransformer
import org.gradle.plugins.ide.idea.model.*
import org.gradle.plugins.ide.internal.generator.XmlPersistableConfigurationObject
import org.gradle.util.DeprecationLogger
import org.gradlefx.conventions.FlexType

/**
 * Represents the customizable elements of an iml (via XML hooks everything of the iml is customizable).
 *
 * @author Hans Dockter
 */
class BuildConfiguration {

    String name
    FlexType flexType
    Path outputFile
    Path outputDir
    String flexSdk
    String targetPlayer
    HashSet compilerOptions


    BuildConfiguration(String name,
            FlexType flexType,
            Path outputFile,
            Path outputDir,
            String flexSdk,
            String targetPlayer,
            HashSet compilerOptions) {
        this.name = name
        this.flexType = flexType
        this.outputDir = outputDir
        this.outputFile = outputFile
        this.flexSdk = flexSdk
        this.targetPlayer = targetPlayer
        this.compilerOptions = compilerOptions
    }

    void addToNode(Node parentNode) {

//        Node libraryNode = parentNode.appendNode('orderEntry', [type: 'module-library'] + (exported ? [exported: ""] : [:])).appendNode('library')
//        Node classesNode = libraryNode.appendNode('CLASSES')
//        Node javadocNode = libraryNode.appendNode('JAVADOC')
//        Node sourcesNode = libraryNode.appendNode('SOURCES')
//        swcs.each { Path path ->
//            classesNode.appendNode('root', [url: path.url])
//        }
//        asdoc.each { Path path ->
//            javadocNode.appendNode('root', [url: path.url])
//        }
//        sources.each { Path path ->
//            sourcesNode.appendNode('root', [url: path.url])
//        }
//        swcDirectories.each { JarDirectory jarDirectory ->
//            libraryNode.appendNode('jarDirectory', [url: jarDirectory.path.url, recursive: jarDirectory.recursive])
//        }
    }

    boolean equals(o) {
        if (this.is(o)) { return true }

        if (getClass() != o.class) { return false }

        BuildConfiguration that = (BuildConfiguration) o;

        if (name != that.name) { return false }
        if (flexType != that.flexType ) { return false }
        if (outputDir != that.outputDir) { return false }
        if (outputFile != that.outputFile) { return false }
        if (flexSdk != that.flexSdk) { return false }
        if (targetPlayer != that.targetPlayer) { return false }
        if (compilerOptions != that.compilerOptions) { return false }

        return true;
    }

    int hashCode() {
        int result;

        result = name.hashCode();
        result = 31 * result + flexType.hashCode();
        result = 31 * result + outputDir.hashCode();
        result = 31 * result + outputFile.hashCode();
        result = 31 * result + flexSdk.hashCode();
        result = 31 * result + targetPlayer.hashCode();
        result = 31 * result + compilerOptions.hashCode();
        return result;
    }

    public String toString() {
        return "BuildConfiguration{" +
                "name=" + name +
                ", flexType=" + flexType +
                ", outputDir=" + outputDir +
                ", outputFile=" + outputFile +
                ", flexSdk=" + flexSdk +
                ", targetPlayer=" + targetPlayer +
                ", compilerOptions=" + compilerOptions +
                '}';
    }
}
