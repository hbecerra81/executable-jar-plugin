package org.lain.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

class ExecutableJarPlugin implements Plugin<Project> {
    String jarPath;

    ExecutableJarPlugin() {
         jarPath = new File(ExecutableJarPlugin.class.protectionDomain.codeSource.location.toURI()).absolutePath
    }

    void apply(Project project) {

        if(!project.ext.hasProperty('mainClassName')){
            project.ext.mainClassName = null
        }

        project.task('compileJarInJarLoader', type: JavaCompile) {
            options.fork = true
            options.warnings = false
            source project.zipTree(jarPath).matching({
                include 'META-INF/jarinjarloader/**/*.java'
            })
        }

        project.task('executableJar', type: Jar) {
            doFirst {
                from project.configurations.runtime.files
                destinationDir = new File(project.buildDir, "app")

                manifest {
                    attributes  project.jar.manifest.attributes
                    attributes  'Rsrc-Main-Class':  project.mainClassName,
                                'Main-Class':       'org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader',
                                'Rsrc-Class-Path':  ['./', *(project.configurations.runtime.files*.name)].join(' '),
                                'Class-Path':       '.'
                }
                from(project.jar.source) { exclude('MANIFEST.MF') }
            }
        }

        project.afterEvaluate {
            project.compileJarInJarLoader.destinationDir = project.compileJava.destinationDir
            project.compileJarInJarLoader.classpath = project.files()

            project.compileJava.finalizedBy project.compileJarInJarLoader
            project.jar.finalizedBy project.executableJar

        }
    }
}
