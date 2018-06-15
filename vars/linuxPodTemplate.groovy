#!/usr/bin/groovy
def call(Map params = [:], body) {
  //TODO: Add liveness probes to container? 
  // containerLivenessProbe(
    // execArgs: 'some --command',
    // initialDelaySeconds: 30,
    // timeoutSeconds: 1,
    // failureThreshold: 3,
    // periodSeconds: 10,
    // successThreshold: 1)

  def label = buildId()

  def containerTemplates  = []
  def idleMinutes         = params.idleMinutes ?: 20
  def imagePullSecrets    = []
  def volumes             = []
  def workspaceVolume     = []

  def nodeSelector  = 'Role=Application.Linux'
  def jnlpImage     = 'jenkinsci/jnlp-slave:3.19-1-alpine'
  def dockerImage   = 'docker:18.03'
  def helmImage     = 'campbelldgunn/k8s-helm:v2.9.0'
  def kubectlImage  = 'campbelldgunn/k8s-kubectl:v1.10.0'

  containerTemplates.add(
    containerTemplate(
      name                  : 'jnlp',
      image                 : "${jnlpImage}",
      args                  : '${computer.jnlpmac} ${computer.name}',
      workingDir            : '/home/jenkins',
      resourceRequestCpu    : '200m',
      resourceLimitCpu      : '200m',
      resourceRequestMemory : '128Mi',
      resourceLimitMemory   : '512Mi'
    ))
  containerTemplates.add(
    containerTemplate(
      name       : 'docker',
      image      : "${dockerImage}",
      command    : 'cat',
      ttyEnabled : true
    ))
  containerTemplates.add(
    containerTemplate(
      name       : 'helm',
      image      : "${helmImage}",
      command    : 'cat',
      ttyEnabled : true
    ))
  containerTemplates.add(
    containerTemplate(
      name       : 'kubectl',
      image      : "${kubectlImage}",
      command    : 'cat',
      ttyEnabled : true
    ))

  if(params.name && params.name != '') {
    def containerCommand    = params.command ?: 'cat'
    def containerName       = params.name
    def containerImage      = params.image
    def containerTtyEnabled = params.ttyEnabled ?: true
  
    containerTemplates.add(
      containerTemplate(
        name       : "${containerName}",
        image      : "${containerImage}",
        command    : "${containerCommand}",
        ttyEnabled : containerTtyEnabled
      ))
  }

  imagePullSecrets.add('dfsacr_creds')
  imagePullSecrets.add('adacr_creds')
  imagePullSecrets.add('artifactory_deployment')

  volumes.add(
    hostPathVolume(
      mountPath: '/var/run/docker.sock',
      hostPath: '/var/run/docker.sock'
    ))

  podTemplate(label: label,
    nodeSelector     : "${nodeSelector}",
    idleMinutesStr   : "${idleMinutes}",
    containers       : containerTemplates,
    volumes          : volumes,
    imagePullSecrets : imagePullSecrets
  ) {
    body()
  }
}
