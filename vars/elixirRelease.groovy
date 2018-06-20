#!/usr/bin/groovy
def call(Map config, List tags, String acct. body) {
  //TODO:
  //Implement Service Now portion
  //If test deployment do not run service now

  def version_tag = tags.get(0)

  standardRelease(config, version_tag){}

  body()
}
