#!/usr/bin/groovy
def call(Map config, List tags, String acct) {
  def utils = new com.activedisclosure.Utils()

  def creds_id    = config.container_repo.jenkins_creds_id
  def dockerfile  = config.container_repo.dockerfile
  def host        = config.container_repo.host
  def repo        = config.container_repo.repo


  container('docker') {
    withCredentials([[
      $class: 'UsernamePasswordMultiBinding',
      credentialsId: creds_id,
      usernameVariable: 'USERNAME',
      passwordVariable: 'PASSWORD']]) {
        sh "docker login -u ${env.USERNAME} -p ${env.PASSWORD} ${host}"
    }

    println "Running Docker build/publish: ${host}/${acct}/${repo}:${tags}"

    docker.withRegistry("https://${host}", "${creds_id}") {
      def tag   = tags.get(0)
      def name  = "${host}/${acct}/${repo}:${tag}"
      def image = docker.image(name)

      sh """
      docker build \\
          --build-arg VCS_REF=${env.GIT_COMMIT_ID} \\
          --build-arg BUILD_DATE=`date -u +'%Y-%m-%dT%H:%M:%SZ'` \\
          -t '${name}' ${dockerfile}
      """

      for (int i = 0; i < tags.size(); i++) {
          image.push(tags.get(i))
      }

      return image.id
    }  
  }
}
