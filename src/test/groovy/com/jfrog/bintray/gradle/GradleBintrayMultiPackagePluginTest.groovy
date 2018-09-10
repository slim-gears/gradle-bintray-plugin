package com.jfrog.bintray.gradle

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import spock.lang.Specification

class GradleBintrayMultiPackagePluginSpec extends Specification {
    @Rule public TestName testName = new TestName()
    @Rule public TemporaryFolder testProjectDir = new TemporaryFolder()

    private Project project
    private def config = TestsConfig.getInstance().config

    def setup() {
        project = ProjectBuilder.builder()
                .withName('testProject')
                .withProjectDir(testProjectDir.root)
                .build()

        project.apply(plugin: 'maven-publish')
        project.apply(plugin: 'com.jfrog.bintray.multipackage')
    }

    void "[publication] multi-package publication"() {
        when:
        project.publishing {
            publications {
                testPublication1 {
                    groupId = 'group1'
                    artifactId = 'artifact1'
                }
                testPublication2 {
                    groupId = 'group2'
                    artifactId = 'artifact2'
                }
            }
        }

        project.bintray {
            user = config.bintrayUser
            key = config.bintrayKey
            dryRun = true
            pkg {
                repo = 'repo1'
                name = 'test-package1'
                publications = ['testPublication1']
            }
            pkg {
                repo = 'repo2'
                name = 'test-package2'
                publications = ['testPublication2']
            }
        }

        project.evaluate()

        then:
        Assert.assertEquals(2, project.bintray.packages.size())
        Assert.assertNotNull(project.tasks.findByName('test-package1BintrayUpload'))
        Assert.assertNotNull(project.tasks.findByName('test-package2BintrayUpload'))

        def pkg1UploadTask = project.tasks.findByName('test-package1BintrayUpload') as BintrayUploadTask
        def pkg2UploadTask = project.tasks.findByName('test-package2BintrayUpload') as BintrayUploadTask

        Assert.assertEquals('test-package1', pkg1UploadTask.packageName)
        Assert.assertEquals('repo1', pkg1UploadTask.repoName)
        Assert.assertEquals('test-package2', pkg2UploadTask.packageName)
        Assert.assertEquals('repo2', pkg2UploadTask.repoName)
    }
}
