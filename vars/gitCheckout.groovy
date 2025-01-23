def call(Map params = [:]) {

        checkout([$class: 'GitSCM',
                  branches: [[name: "${params.BRANCH}"]],
                  doGenerateSubmoduleConfigurations: false,
                  extensions: [],
                  gitTool: 'Default',
                  submoduleCfg: [],
                  userRemoteConfigs: [[credentialsId: "${params.authId}",url: "${params.gitAddr}"]]
                ])
        def commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
        currentBuild.description = "Commit: ${commitMessage}"
        sh "git rev-parse --short HEAD > ./.COMMIT-VERSION"

}