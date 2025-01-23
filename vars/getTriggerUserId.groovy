def call() {
    def buildCauses = currentBuild.rawBuild.getCauses()
    for (cause in buildCauses) {
        if (cause.class.simpleName == 'UserIdCause') {
            return cause.getUserId()
        }
    }
    return 'Unknown'
}