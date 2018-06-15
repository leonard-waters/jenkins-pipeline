#!/usr/bin/groovy
def call(Map config = [:], List tags = [], String acct = '', body) {
  stage('install dependencies') {
    container('nodejs-build') {
      sh "yarn install; yarn run build"
      sh "yarn install --production"
    }
  }

  stage('test') {

  }

  stage('build release') {
    
  }

  body()
}
