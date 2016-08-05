package org.lain.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class ExecutableJarPlugin implements Plugin<Project> {
    String jarPath;
    String jarName;

    ExecutableJarPlugin() {
         File file = new java.io.File(ExecutableJarPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath());
         jarPath = file.absolutePath
         jarName = file.name
    }

    void apply(Project project) {
        project.ext.mainClassName = null
        def jarinjarSrc = project.zipTree(jarPath).matching({
            include 'META-INF/jarinjarloader/**/*.java'
        })

        project.compileJava.source jarinjarSrc

        project.task('executableJar', type: Jar) {
            doFirst {
                from project.configurations.runtime.files
                destinationDir = new File(project.buildDir, "dist")
                archiveName = "$project.name.$extension"

                manifest {
                    attributes project.jar.manifest.attributes
                    attributes  'Rsrc-Main-Class':  project.mainClassName,
                                'Main-Class':       'org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader',
                                'Rsrc-Class-Path':  ['./', *(project.configurations.runtime.files*.name)].join(' '),
                                'Class-Path':       '.'
                }
                from(project.jar.source) { exclude('MANIFEST.MF') }
            }
        }

        project.jar.finalizedBy project.executableJar
    }
}
