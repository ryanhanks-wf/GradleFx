package org.gradlefx.plugins.ide.idea

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: ryanhanks
 * Date: 1/20/13
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
class IdeafxPlugin implements Plugin<Project> {
    public void apply(Project project){
        project.apply(plugin: 'idea')
    }
}
