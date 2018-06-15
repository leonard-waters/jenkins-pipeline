#!/usr/bin/groovy
def call(Map config, List tags, String acct) {
  //TODO:
  //Implement Service Now portion
  //If test deployment do not run service now

  def version_tag = tags.get(0)
  
  elixirCI(config, tags, acct){}

  standardRelease(config, version_tag){}
}
