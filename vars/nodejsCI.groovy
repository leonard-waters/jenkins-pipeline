#!/usr/bin/groovy
//This template is very specific to React applications with Webpack
def call(Map config = [:], List tags = [], String acct = '', body) {
  stage('install dependencies') {
    container('nodejs-build') {
      sh 'yarn version --no-git-tag-version'
      sh 'yarn install;'
    }
  }

  stage('lint') {
    try {
      sh 'yarn lint -f junit -o lint-report.xml'
    } finally {
      junit 'lint-report.xml'
    }
  }

  stage('test') {
    try {
      sh 'yarn add jest jest-junit --dev'
      withEnv(["JEST_JUNIT_OUTPUT=./jest-test-results.xml"]) {
				sh 'yarn test --ci --testResultsProcessor="jest-junit"'
			}
    } finally {
      junit 'unit-test-report.xml'
    }
  }

  stage('build release') {
    sh 'webpack --config webpack.prod.js --bail --display-error-details'
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
