def call(String prefix = '') {
  def BRANCH_NAME = env.GIT_BRANCH.replaceFirst(/^[a-z]*\//, "")
  return "${prefix}${BRANCH_NAME}_${env.BUILD_NUMBER}".replaceAll('[^A-Za-z0-9]', '_')
}
