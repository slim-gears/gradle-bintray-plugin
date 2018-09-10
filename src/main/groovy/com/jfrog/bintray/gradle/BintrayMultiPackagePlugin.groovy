package com.jfrog.bintray.gradle

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class BintrayMultiPackagePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('bintray', BintrayExtension, project)
        extension.apiUrl = BintrayUploadTask.API_URL_DEFAULT

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                super.projectsEvaluated(gradle)
                def bintrayUploadTask = project.task('bintrayUpload', group: 'publishing')
                extension.packages.each { pkg ->
                    def taskName = "${pkg.name}BintrayUpload"
                    BintrayUploadTask packageUploadTask = project.tasks.create(name: taskName, type: BintrayUploadTask) as BintrayUploadTask
                    packageUploadTask.project = project
                    packageUploadTask.fromExtension(extension, pkg)
                    bintrayUploadTask.dependsOn(packageUploadTask)
                }
            }
        })
    }
}
