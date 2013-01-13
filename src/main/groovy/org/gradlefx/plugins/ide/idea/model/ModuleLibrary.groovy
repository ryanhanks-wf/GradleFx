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

import org.gradle.plugins.ide.idea.model.Dependency
import org.gradle.plugins.ide.idea.model.JarDirectory
import org.gradle.plugins.ide.idea.model.Path

/**
 * Represents an orderEntry of type module-library in the iml xml.
 *
 * @author Hans Dockter
 */
class ModuleLibrary implements Dependency {
    /**
     * A set of {@link org.gradle.plugins.ide.idea.model.Path} instances for class libraries. Can be paths to jars or class folders.
     */
    Set<Path> swcs

    /**
     * A set of {@link org.gradle.plugins.ide.idea.model.JarDirectory} instances for directories containing jars.
     */
    Set<JarDirectory> swcDirectories

    /**
     * A set of {@link org.gradle.plugins.ide.idea.model.Path} instances for asdoc associated with the library elements.
     */
    Set<Path> asdoc

    /**
     * A set of {@link org.gradle.plugins.ide.idea.model.Path} instances for source code associated with the library elements.
     */
    Set<Path> sources

    /**
     * The scope of this dependency. If <tt>null</tt>, the scope attribute is not added.
     */
    String scope

    boolean exported

    def ModuleLibrary(Collection<Path> swcs, Collection<Path> asdoc, Collection<Path> sources, Collection<JarDirectory> swcDirectories) {
        this.swcs = swcs as Set;
        this.swcDirectories = swcDirectories as Set;
        this.asdoc = asdoc as Set;
        this.sources = sources as Set;
//        this.scope = scope
//        this.exported = !scope || scope == 'COMPILE' || scope == 'RUNTIME'
    }

    void addToNode(Node parentNode) {
        Node libraryNode = parentNode.appendNode('orderEntry', [type: 'module-library'] + (exported ? [exported: ""] : [:])).appendNode('library')
        Node classesNode = libraryNode.appendNode('CLASSES')
        Node javadocNode = libraryNode.appendNode('JAVADOC')
        Node sourcesNode = libraryNode.appendNode('SOURCES')
        swcs.each { Path path ->
            classesNode.appendNode('root', [url: path.url])
        }
        asdoc.each { Path path ->
            javadocNode.appendNode('root', [url: path.url])
        }
        sources.each { Path path ->
            sourcesNode.appendNode('root', [url: path.url])
        }
        swcDirectories.each { JarDirectory jarDirectory ->
            libraryNode.appendNode('jarDirectory', [url: jarDirectory.path.url, recursive: jarDirectory.recursive])
        }
    }

    boolean equals(o) {
        if (this.is(o)) { return true }

        if (getClass() != o.class) { return false }

        ModuleLibrary that = (ModuleLibrary) o;

        if (swcs != that.swcs) { return false }
        if (swcDirectories != that.swcDirectories) { return false }
        if (asdoc != that.asdoc) { return false }
        if (sources != that.sources) { return false }

        return true;
    }

    private boolean scopeEquals(String lhs, String rhs) {
        if (lhs == 'COMPILE') {
            return !rhs || rhs == 'COMPILE'
        } else if (rhs == 'COMPILE') {
            return !lhs || lhs == 'COMPILE'
        } else {
            return lhs == rhs
        }
    }


    int hashCode() {
        int result;

        result = swcs.hashCode();
        result = 31 * result + swcDirectories.hashCode();
        result = 31 * result + asdoc.hashCode();
        result = 31 * result + sources.hashCode();
        result = 31 * result + getScopeHash()
        return result;
    }

    private int getScopeHash() {
        (scope && scope != 'COMPILE' ? scope.hashCode() : 0)
    }

    public String toString() {
        return "ModuleLibrary{" +
                "swcs=" + swcs +
                ", swcDirectories=" + swcDirectories +
                ", asdoc=" + asdoc +
                ", sources=" + sources +
                ", scope='" + scope + '\'' +
                '}';
    }
}
