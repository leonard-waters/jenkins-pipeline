#!/usr/bin/groovy
package org.whiteshieldinc;

def kubectlTest() {
    // Test that kubectl can correctly communication with the Kubernetes API
    sh "kubectl get nodes"

}

def helmLint(String chart_dir) {
    // lint helm chart
    sh "helm lint ${chart_dir}"

}

def helmConfig() {
    //setup helm connectivity to Kubernetes API and Tiller
    sh "helm init"
    sh "helm version"
}


def helmDeploy(Map args) {
    //configure helm client and confirm tiller process is installed
    helmConfig()

    def String namespace

    // If namespace isn't parsed into the function set the namespace to the name
    if (args.namespace == null) {
        namespace = args.name
    } else {
        namespace = args.namespace
    }

    if (args.dry_run) {
        sh "helm upgrade --dry-run --install ${args.name} ${args.chart_dir} --set imageTag=${args.version_tag},replicas=${args.replicas},cpu=${args.cpu},memory=${args.memory},ingress.hostname=${args.hostname} --namespace=${namespace}"
    } else {
        // reimplement --wait once it works reliable
        sh "helm upgrade --install ${args.name} ${args.chart_dir} --set imageTag=${args.version_tag},replicas=${args.replicas},cpu=${args.cpu},memory=${args.memory},ingress.hostname=${args.hostname} --namespace=${namespace}"

        // sleeping until --wait works reliably
        sleep(20)

        echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
    }
}

def helmDelete(Map args) {
        sh "helm delete ${args.name}"
}

def helmTest(Map args) {
    sh "helm test ${args.name} --cleanup"
}

def gitEnvVars() {
    // create git envvars
    sh 'git rev-parse HEAD > git_commit_id.txt'
    try {
        env.GIT_COMMIT_ID = readFile('git_commit_id.txt').trim()
        env.GIT_SHA = env.GIT_COMMIT_ID.substring(0, 7)
    } catch (e) {
        error "${e}"
    }

    sh 'git config --get remote.origin.url> git_remote_origin_url.txt'
    try {
        env.GIT_REMOTE_URL = readFile('git_remote_origin_url.txt').trim()
    } catch (e) {
        error "${e}"
    }
}

def containerBuildPub(Map args) {
    println "Running Docker build/publish: ${args.host}/${args.acct}/${args.repo}:${args.tags}"

    docker.withRegistry("https://${args.host}", "${args.auth_id}") {

        def tag = args.tags.get(0)
        def name = "${args.host}/${args.acct}/${args.repo}:${tag}"
        def img = docker.image(name)
        sh "docker build --build-arg VCS_REF=${env.GIT_SHA} --build-arg BUILD_DATE=`date -u +'%Y-%m-%dT%H:%M:%SZ'` -t '${name}' ${args.dockerfile}"

        for (int i = 0; i < args.tags.size(); i++) {
            img.push(args.tags.get(i))
        }

        return img.id
    }
}

def getContainerTags(config, Map tags = [:]) {

    def String commit_tag

    try {
        // if branch available, use as prefix, otherwise only commit hash
        if (env.BRANCH_NAME) {
            commit_tag = env.BRANCH_NAME + '-' + env.GIT_COMMIT_ID.substring(0, 7)
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

    for (int i=0; i < entries.size(); i++){
        String value =  entries.get(i).value
        tag_list.add(value.replace("features/", ""))
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
