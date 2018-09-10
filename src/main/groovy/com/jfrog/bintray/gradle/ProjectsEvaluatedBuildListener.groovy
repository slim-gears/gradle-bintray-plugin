package com.jfrog.bintray.gradle

import com.jfrog.bintray.gradle.tasks.BintrayPublishTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Upload

import java.util.concurrent.ConcurrentHashMap

class ProjectsEvaluatedBuildListener extends BuildAdapter implements ProjectEvaluationListener {
    private BintrayUploadTask bintrayUpload
    private final Set<BintrayUploadTask> bintrayUploadTasks = Collections.newSetFromMap(new ConcurrentHashMap<Task, Boolean>());
    private final BintrayExtension extension;

    ProjectsEvaluatedBuildListener(BintrayUploadTask bintrayUpload) {
        this.bintrayUpload = bintrayUpload
        this.extension = bintrayUpload.project.extensions.create("bintray", BintrayExtension, bintrayUpload.project)
        this.extension.with {
            apiUrl = BintrayUploadTask.API_URL_DEFAULT
        }
    }

    @Override
    void beforeEvaluate(Project project) {
    }

    @Override
    void afterEvaluate(Project proj, ProjectState state) {
        bintrayUploadTasks.add(bintrayUpload)
        Task bintrayPublish = bintrayUpload.project.getRootProject().getTasks().findByName(BintrayPublishTask.TASK_NAME)
        if (bintrayPublish == null) {
            throw new IllegalStateException(String.format("Could not find %s in the root project", BintrayPublishTask.TASK_NAME))
        }
        bintrayUpload.finalizedBy(bintrayPublish)
        // Depend on tasks in sub-projects
        bintrayUpload.project.subprojects.each {
            Task subTask = it.tasks.findByName(BintrayUploadTask.TASK_NAME)
            if (subTask) {
                bintrayUpload.dependsOn(subTask)
            }
        }
        if (this.extension.filesSpec) {
            bintrayUpload.dependsOn(this.extension.filesSpec)
        }
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
        for (BintrayUploadTask bintrayUpload : bintrayUploadTasks) {
            bintrayUpload.fromExtension(this.extension, this.extension.pkg)
        }
    }
}
