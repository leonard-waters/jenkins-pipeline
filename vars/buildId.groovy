#!/usr/bin/groovy
def call(String prefix = '') {
  def job_name = env.JOB_NAME.replaceAll('%2F', '_')
  def idString = "${prefix}${job_name}_${env.BUILD_NUMBER}".replaceAll('-', '_').replaceAll('/', '_').replaceAll(' ', '_')
  println "Using Build ID ${idString}"
  return idString
}
