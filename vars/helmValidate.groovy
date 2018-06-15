def call(Map config, String version_tag) {
  container('helm') {
    def utils = new com.activedisclosure.Utils()
    def pwd = pwd()

    def name = config.app.name
    def chart_dir = "${pwd}/charts/${name}"

    utils.helmLint(chart_dir)

    utils.helmDeploy(
      chart_dir   : chart_dir,
      cpu         : config.app.cpu,
      dry_run     : true,
      hostname    : config.app.hostname,
      memory      : config.app.memory,
      name        : name,
      namespace   : config.app.namespace,
      replicas    : config.app.replicas,
      version_tag : version_tag
    )
  }
}
