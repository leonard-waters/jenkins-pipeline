#!/usr/bin/groovy
def call(Map config = [:], List tags = [], String acct = '', body) {
  stage ('install dependencies') {
    container('elixir-build') {
      sh """
      mix do local.hex --force, local.rebar --force
      mix do deps.get
      """
    }
  }

  stage ('test') {
    container('elixir-build') {
      try {
        sh "mix test"
      }
      finally {
        junit '_build/test/**/*test.xml'
      }
    }
  }

  stage ('build release') {
    container('elixir-build') {
      sh "MIX_ENV=prod mix release --env prod"
    }
  }

  stage('validate helm chart') {
    def version_tag = tags.get(0)
    
    helmValidate(config, version_tag)
  }

  stage('docker build and publish') {
    dockerBuildAndPublish(config, tags, acct)
  }

  body()
}
