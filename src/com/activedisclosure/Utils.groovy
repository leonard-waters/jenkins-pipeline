#!/usr/bin/groovy
package com.activedisclosure

def getBranch() {
    def branch = env.BRANCH_NAME
    if (!branch) {
        try {
            branch = sh(script: 'git symbolic-ref --short HEAD', returnStdout: true).toString().trim()
        } catch (err) {
            echo('Unable to get git branch and in a detached HEAD. You may need to select Pipeline additional behaviour and \'Check out to specific local branch\'')
            return null
        }
    }
    echo "Using branch ${branch}"
    return branch
}

def getContainerTags(config, Map tags = [:]) {
    def String commit_tag

    gitEnvVars()

    def current_branch = getBranch()
    println "Result of getBranch func is: ${current_branch}"

    try {
        // if branch available, use as prefix, otherwise only commit hash
        if (current_branch) {
            commit_tag = current_branch + '-' + env.GIT_COMMIT_ID.substring(0, 7)
        } else {
            commit_tag = env.GIT_COMMIT_ID.substring(0, 7)
        }
        tags << ['commit': commit_tag]
    } catch (Exception e) {
        println "WARNING: commit unavailable from env. ${e}"
    }

    try {
        if (env.BRANCH_NAME != 'master') {
            tags << ['branch': env.BRANCH_NAME]
        } else {
            tags << ['master': 'latest']
        }
    } catch (Exception e) {
        println "WARNING: commit unavailable from env. ${e}"
    }

    // build tag only if none of the above are available
    if (!tags) {
        try {
            tags << ['build': env.BUILD_TAG]
        } catch (Exception e) {
            println "WARNING: build tag unavailable from config.project. ${e}"
        }
    }

    def entries = []
    def tag_list = []

    entries.addAll(tags.entrySet())

    for (int i = 0; i < entries.size(); i++) {
        String value = entries.get(i).value
        tag_list.add(value.replaceFirst(/^[a-z]*\//, ""))
    }

    return tag_list
}

def getContainerRepoAcct(config) {
    def String acct

    if (env.BRANCH_NAME == 'master') {
        acct = config.container_repo.master_acct
    } else {
        acct = config.container_repo.alt_acct
    }

    return acct
}

// helper to get the repo name from the job name when using org + branch github plugins
def getRepoName() {
    def jobName = env.JOB_NAME

    // job name from the org plugin
    if (jobName.count('/') > 1) {
        return jobName.substring(jobName.indexOf('/') + 1, jobName.lastIndexOf('/'))
    }
    // job name from the branch plugin
    if (jobName.count('/') > 0) {
        return jobName.substring(0, jobName.lastIndexOf('/'))
    }
    // normal job name
    return jobName
}

def gitEnvVars() {
    // create git envvars
    try {
        env.GIT_COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).toString().trim()

        println "Commit ID is: ${env.GIT_COMMIT_ID}"
    } catch (e) {
        error "${e}"
    }

    try {
        env.GIT_REMOTE_URL = sh(script: 'git config --get remote.origin.url', returnStdout: true).toString().trim()
        println "Remote URL is: ${env.GIT_REMOTE_URL}"
    } catch (e) {
        error "${e}"
    }
}

def helmDelete(String name) {
    sh "helm delete --timeout 300 --purge ${name}"
}

def helmDeploy(Map args) {
  //configure helm client and confirm tiller process is installed
  sh "helm init"
  sh "helm version"

  def setValues = "imageTag=${args.version_tag},replicas=${args.replicas},cpu=${args.cpu},memory=${args.memory},ingress.hostname=${args.hostname}"

  if (args.dry_run) {
    sh "helm upgrade --dry-run --install ${args.name} ${args.chart_dir} \
      --set ${setValues} \
      --namespace=${args.namespace}"
  } else {
    sh "helm upgrade --install ${args.name} ${args.chart_dir} \
      --set ${setValues} \
      --namespace=${args.namespace}"

    echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
  }
}

def helmLint(String chart_dir) {
    sh "helm lint ${chart_dir}"
}

def helmTest(String name) {
    sh "helm test ${name} --cleanup"
}

def isCI() {
    def branch = getBranch()
    if (branch && branch != 'master') {
        return true
    }
    // if we can't get the branch assume we're not in a CI pipeline as that would be a PR branch
    return false
}

def isCD() {
    def branch = getBranch()
    if (!branch || branch.equals('master')) {
        return true
    }
    // if we can't get the branch assume we're not in a CI pipeline as that would be a PR branch
    return false
}

def parseConfig(String config_path) {
  try {
    def inputFile = readFile(config_path)
    def config = new groovy.json.JsonSlurperClassic().parseText(inputFile)
    println "pipeline config ==> ${config}"

    return config
  } catch (Exception ex) {
    echo "Error parsing Jenkinsfile config"
  }
}
