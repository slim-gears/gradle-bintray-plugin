package com.jfrog.bintray.gradle

import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class BintrayExtension {

    Project project

    String apiUrl

    String user

    String key

    List<PackageConfig> packages = []

    String[] configurations

    String[] publications

    RecordingCopyTask filesSpec

    boolean publish

    boolean override

    boolean dryRun

    BintrayExtension(Project project) {
        this.project = project
    }

    def getPkg() {
        packages.isEmpty() ? null : packages.first()
    }

    def pkg(Closure<PackageConfig> closure) {
        def pkg = new PackageConfig()
        ConfigureUtil.configure(closure, pkg)
        packages.add(pkg)
        pkg
    }

    def filesSpec(Closure closure) {
        filesSpec = project.task(type: RecordingCopyTask, RecordingCopyTask.NAME)
        ConfigureUtil.configure(closure, filesSpec)
        filesSpec.outputs.upToDateWhen { false }
    }

    class PackageConfig {
        String[] publications
        String repo
        //An alternative user for the package
        String userOrg
        String name
        String desc
        String websiteUrl
        String issueTrackerUrl
        String vcsUrl
        String githubRepo
        String githubReleaseNotesFile
        boolean publicDownloadNumbers
        String[] licenses
        String[] labels
        Map attributes

        VersionConfig version = new VersionConfig()
        def version(Closure closure) {
            ConfigureUtil.configure(closure, version)
        }

        DebianConfig debian = new DebianConfig()
        def debian(Closure closure) {
            ConfigureUtil.configure(closure, debian)
        }
    }

    class DebianConfig {
        String distribution
        String component
        String architecture
    }

    class VersionConfig {
        String name
        String desc
        String released
        String vcsTag
        Map attributes

        GpgConfig gpg = new GpgConfig()
        def gpg(Closure closure) {
            ConfigureUtil.configure(closure, gpg)
        }

        MavenCentralSyncConfig mavenCentralSync = new MavenCentralSyncConfig()
        def mavenCentralSync(Closure closure) {
            ConfigureUtil.configure(closure, mavenCentralSync)
        }
    }

    class GpgConfig {
        boolean sign
        String passphrase
    }

    class MavenCentralSyncConfig {
        Boolean sync
        String user
        String password
        String close
    }
}