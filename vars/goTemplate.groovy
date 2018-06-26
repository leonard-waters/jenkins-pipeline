#!/usr/bin/groovy
def call(body) {
  def label = buildId()

  def goVersion = '1.10.3'
  def alpineVersion = '3.7'

  linuxPodTemplate(name: 'golang-build', image: "golang:${goVersion}-alpine${alpineVersion}") {
    node(label) {
      body()
    }
  }
}
