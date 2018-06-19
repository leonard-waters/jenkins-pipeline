#!/usr/bin/groovy

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def message = config.message ?: 'Approve for Production Deployment?'
  def okText = config.okText ?: 'Deploy'
  def timeoutTime = config.timeoutTime ?: 20

  timeout(time: timeoutTime, unit: 'MINUTES') {
    input(message: message, ok: okText)
  }
}
