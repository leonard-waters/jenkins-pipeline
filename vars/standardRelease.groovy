#!/usr/bin/groovy
def call(Map config, String version_tag, body) {
  //TODO:
  //Implement Service Now portion
  //If test deployment do not run service now
  
  def utils = new com.activedisclosure.Utils()

  def hostname  = config.app.hostname
  def is_test   = config.isTest ?: false
  def name      = config.app.name
  def namespace = config.app.namespace 
  def replicas  = config.app.replicas

  def pwd = pwd()
  def chart_dir = "${pwd}/charts/${name}"

  if (is_test) {
    def commit  = env.GIT_COMMIT_ID
    name        = "sha-${commit}-${config.app.name}"
    hostname    = "sha-${commit}-${config.app.hostname}"
    replicas    = 1
  }

  container('helm') {
    // Deploy using Helm chart.
    utils.helmDeploy(
      chart_dir   : chart_dir,
      cpu         : config.app.cpu,
      dry_run     : is_test,
      hostname    : hostname,
      memory      : config.app.memory,
      name        : name,
      namespace   : namespace,
      replicas    : replicas,
      version_tag : version_tag
    )

    sh "helm status ${name}"

    if (is_test) {
      // utils.helmTest(name)
      utils.helmDelete(name)
    }
  }

  body()
}
