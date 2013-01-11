package org.gradlefx.plugins.ide.idea

import org.gradle.plugins.ide.api.XmlGeneratorTask
import org.gradlefx.plugins.ide.idea.model.Module
import org.gradlefx.plugins.ide.idea.model.IdeaModule

/**
 * Created with IntelliJ IDEA.
 * User: ryanhanks
 * Date: 1/9/13
 * Time: 11:06 PM
 * To change this template use File | Settings | File Templates.
 */
class GenerateIdeaModule extends XmlGeneratorTask<Module> {
    IdeaModule module

    @Override protected Module create() {
        new Module(xmlTransformer, module.pathFactory)
    }

    @Override protected void configure(Module xmlModule) {
        getModule().mergeXmlModule(xmlModule)
    }

    File getOutputFile() {
        return module.outputFile
    }

    void setOutputFile(File newOutputFile) {
        module.outputFile = newOutputFile
    }

}