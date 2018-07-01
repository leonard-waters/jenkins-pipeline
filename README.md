# DFS Pipeline Library

This git repository contains a library of reusable [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) steps and functions that can be used in your Jenkinsfile to help improve your Continuous Integration and Continuous Delivery pipeline.

## How to use this library

To use the functions in this library just add the following to the top of your Jenkinsfile:

``` groovy
@Library('github.com/dfs-activedisclosure/dfs-pipeline-library@master')
```

That will use the master branch of this library. You can if you wish pick a specific tag or commit SHA of this repository too.

## Commonly used functions from the Jenkins global library

### Approval

This is an input step that blocks the pipeline until timeout, which results in failure, or until a user selects `approve` or `abort`.

``` groovy
stage('approve to production') {
  approval {
    message = 'Deploy to production?'
    okText = 'Deploy'
    timeoutTime = 10
  }
}
```

Default values:

- message: `Approve for Production Deployment?`
- okText: `Deploy`
- timeoutTime: `10` (this is in minutes)

### Standard Release

This is a standardized release function that deploys the project using the helm chart provided in the project's repository.

The function expects the `config` map and the `version_tag`.

``` groovy
stage('deploy to production') {
  def version_tag = tags.get(0)
  standardRelease(config, version_tag) {}
}
```

### Utility Functions

These functions are used by creating a ref:

``` groovy
def utils = new com.activedisclosure.Utils()
```

#### Parse Configuration

This function takes a given [config file](#config-file) path and returns a `map` of the data

``` groovy
utils.parseConfig('Jenkinsfile.json')
```

#### Git Environment Variables

This function assigns the `GIT_COMMIT_ID` and the `GIT_REMOTE_URL` to `env`

``` groovy
utils.gitEnvVars()
```

#### Get Container Repository Account

This function returns the name of the Container Repository from the config `map`

``` groovy
utils.getContainerRepoAcct()
```

#### Get Container Tags

This function returns a list of tags based off the branch

``` groovy
utils.getContainerTags()
```

#### Get Branch Name

This function returns the branch name

``` groovy
utils.getBranch()
```

## Understanding how it works

All of these functions are intended to run inside a Kubernetes Pod. These pods are managed by the [kubernetes plugin](https://github.com/jenkinsci/kubernetes-plugin). This library abstracts the pipeline capabilities of the plugin so that it's easier to use and requires less maintenance.

For example, if I want to use a pod with `golang` capablities it would normally be defined like this:

``` groovy
podTemplate(label: label, nodeSelector: 'beta.kubernetes.io/os=linux',
    containers: [
      containerTemplate(name: 'jnlp', image: 'jenkinsci/jnlp-slave:3.19-1-alpine'),
      containerTemplate(name: 'docker', image: 'docker:18.03'),
      containerTemplate(name: 'golang-build', image: 'golang:1.10.3-alpine3.7'),
      containerTemplate(name: 'helm', image: 'dfs-activedisclosure/k8s-helm:v2.9.0')
  ],
  volumes:[
      hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
  ]){
    ...do CI/CD stuff
  }
```

Now you can use the `goTemplate` provided by this library:

``` groovy
goTemplate {
  stage('do CI stuff') {
    container('golang-build'){
      ...do CI stuff
    }
  }
}
```

The `linuxTemplate` by default includes the `jenkins` agent container, `docker` container, and a `helm` container.

If a stage does not specify what container to use, the `jenkins` agent container is used by default.

### Available Templates

All linux templates use [alpine](https://alpinelinux.org/) build images.

#### Linux Template

Currently (6/29/2018) all other templates build off the `linuxTemplate`

- Golang Template (`goTemplate`)
- NodeJS Template (`nodejsTemplate`)
- Elixir Template (`elixirTemplate`)

### Coming Soon to a library near you`!`

- DotNetCore 2.0 (`netcoreTemplate`)

## Config File

Every Jenkinsfile should have an accompanying configration `JSON` file.

It should be in the following format:

```JSON
{
  "app": {
    "name": "org-broker",
    "namespace": "compliance",
    "replicas": "3",
    "cpu": "10m",
    "memory": "128Mi",
    "hostname": "org-broker.activedisclosure.com"
  },
  "container_repo": {
    "host": "adacr.azurecr.io",
    "master_acct": "compliance",
    "alt_acct": "compliance",
    "jenkins_creds_id": "adacr_creds",
    "repo": "org-broker",
    "dockeremail": ".",
    "dockerfile": "./"
  }
}
```

The `app` object ***must*** contain:

- name
  - `name` is the name of the project as it will be seen in kubernetes
- namespace
  - `namespace` is a scope for `names` and defines the *"virtual clusters"*
  - examples: `compliance`, `activelink`, `kube0-system`
- hostname
  - The `hostname` is the hostname for the project, how it can be reached from outside the cluster or internally
  - examples: `https://broker.activedisclosure.com`,`redis-svc.cluster.local`

Optional are:

- replicas (default: `3`)
  - `Replicas` are the number of `pods` desired to have running at all times
- cpu (default: `10m`)
  - `CPU` is the maximum amount of `millicpu` available for the `container`
- memory (default: `128Mi`)
  - `Memory` is measured in `MebiBytes` and will be the maximum available to the `container`

The `container_repo` requires the following:

- host
- master_acct
- jenkins_creds_id
- repo

Optional are:

- alt_acct
- dockeremail
- dockerfile (default path: `./`)

## This pipeline library is heavily inspired by [Fabric8io's Pipeline Library](https://github.com/fabric8io/fabric8-pipeline-library)
