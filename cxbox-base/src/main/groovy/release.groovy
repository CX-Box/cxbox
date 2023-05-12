/*
 * © OOO "SI IKS LAB", 2022-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



pipeline {

    agent any

    triggers {
        gitlab(
                branchFilterType: 'All',
                triggerOnPush: true,
                triggerOnMergeRequest: false,
                triggerOpenMergeRequestOnPush: "source",
                triggerOnNoteRequest: false,
                noteRegex: "Jenkins please retry a build",
                skipWorkInProgressMergeRequest: true,
                secretToken: 'c2eab9635c9707c670aa08e03814bce8',
                ciSkip: true,
                setBuildDescription: true,
                addNoteOnMergeRequest: true,
                addCiMessage: true,
                addVoteOnMergeRequest: false,
                acceptMergeRequestOnSuccess: false,
                cancelPendingBuildsOnUpdate: true
        )
    }

    parameters {
        string(
                defaultValue: "develop",
                description: 'Ветка GitLab с которой будет идти сборка',
                name: 'branch_name'
        )
        choice(
                description: 'Тип релиза',
                choices: ['interim', 'patch', 'minor', 'major'],
                name: 'release'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                doCheckout()
            }
        }

        stage('Release') {
            steps {
                withMaven(
                        jdk: 'jdk8u172',
                        maven: 'maven-3.5.0',
                        mavenSettingsConfig: 'd191954b-f8c4-41a3-8c9e-3271ec128648',
                        options: [
                                artifactsPublisher(disabled: true)
                        ]
                ) {
                    doRelease()
                }
            }
        }

    }

}

void doRelease() {
    boolean isInterim = "interim" == getReleaseType()
    // получаем актуальный pom.xml со всеми свойствами
    sh("mvn --projects ${getBaseProject()} help:effective-pom -Doutput=${getResultingPom()}")
    if (isInterim) {
        sh("mvn --batch-mode release:clean release:prepare " +
                "-DreleaseVersion=${getReleaseVersion()} " +
                "-DdevelopmentVersion=${getDevelopmentVersion()} " +
                "-DpushChanges=false " +
                "-DpreparationGoals=\"clean deploy\"")
    } else {
        sh("mvn --batch-mode release:clean release:prepare release:perform " +
                "-DreleaseVersion=${getReleaseVersion()} " +
                "-DdevelopmentVersion=${getDevelopmentVersion()} " +
                "-DuseReleaseProfile=false")
    }
}

String getBaseProject() {
    return "cxbox-base"
}

String getResultingPom() {
    return "resultingpom.xml"
}

String getReleaseType() {
    return params.release
}

String getReleaseVersion() {
    return getNextVersion(getProjectVersion(), getReleaseType(), getCommitId(), true)
}

String getDevelopmentVersion() {
    return getNextVersion(getProjectVersion(), getReleaseType(), getCommitId(), false)
}

static String getNextVersion(String currentVersion, String releaseType, String commitId, boolean release) {
    currentVersion = currentVersion.replace("-SNAPSHOT", "")
    String[] parts = currentVersion.split("\\.")
    String[] version = ["0", "0", "0", ""]
    for (int i = 0; i < parts.length; i++) {
        version[i] = parts[i]
    }
    switch (releaseType) {
        case "major":
            version[0] = String.valueOf(Integer.parseInt(version[0]) + 1)
            version[1] = "0"
            version[2] = "0"
            version[3] = ""
            break
        case "minor":
            version[1] = String.valueOf(Integer.parseInt(version[1]) + 1)
            version[2] = "0"
            version[3] = ""
            break
        case "patch":
            version[2] = String.valueOf(Integer.parseInt(version[2]) + 1)
            version[3] = ""
            break
        case "interim":
            version[3] = commitId
            break
    }
    return "${version[0]}.${version[1]}.${version[2]}" + (release && version[3] ? ".${version[3]}" : "") + (release ? "" : "-SNAPSHOT")
}

String getProjectVersion() {
    def pom = readMavenPom file: "${getBaseProject()}/${getResultingPom()}"
    return pom.version
}

String getMavenProperty(String propertyName) {
    def pom = readMavenPom file: "${getBaseProject()}/${getResultingPom()}"
    return pom.properties[propertyName]
}

String getCommitId() {
    return sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
}

def doCheckout() {
    def extensions = [
            [
                    $class             : 'SubmoduleOption',
                    disableSubmodules  : false,
                    parentCredentials  : true,
                    recursiveSubmodules: true,
                    reference          : '',
                    trackingSubmodules : false
            ],
            [
                    $class: 'CleanBeforeCheckout'
            ],
            [
                    $class: 'PruneStaleBranch'
            ],
            [
                    $class: 'WipeWorkspace'
            ],
            [
                    $class: 'CleanCheckout'
            ],
            [
                    $class     : 'LocalBranch',
                    localBranch: "${params.branch_name}"
            ]
    ]
    checkout(changelog: true, poll: true, scm: [
            $class                           : 'GitSCM',
            branches                         : [
                    [
                            name: "*/${params.branch_name}"
                    ]
            ],
            doGenerateSubmoduleConfigurations: false,
            extensions                       : extensions,
            submoduleCfg                     : [],
            userRemoteConfigs                : [
                    [
                            credentialsId: 'jenkins',
                            url          : 'git@gitlab.akb-it.ru:qnt/cxbox.git'
                    ]
            ]
    ])
}
