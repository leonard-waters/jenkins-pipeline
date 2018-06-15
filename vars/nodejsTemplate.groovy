#!/usr/bin/groovy
def call(body) {
  def label = buildId()

  def nodejsVersion = '8'
  def imageHost = 'mhart/alpine-node'

  linuxPodTemplate(name: 'nodejs-build', image: "${imageHost}:${nodejsVersion}") {
    node(label) {
      body()
    }
  }
}
