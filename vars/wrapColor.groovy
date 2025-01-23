def call(Closure body) {
    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
        body()
    }
}