#!/usr/bin/groovy
def call(body) {
  def label = buildId()

  def elixirVersion = '20.3.2-1.6.4'
  def imageHost = 'adacr.azurecr.io/alpine/elixir'

  linuxPodTemplate(name: 'elixir-build', image: "${imageHost}:${elixirVersion}") {
    node(label) {
      body()
    }
  }
}
