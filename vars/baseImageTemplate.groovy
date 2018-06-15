#!/usr/bin/groovy
def call(body) {
  def label = buildId()

  linuxPodTemplate {
    node(label) {
      body()
    }
  }
}
