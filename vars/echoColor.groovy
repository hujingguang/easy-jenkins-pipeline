
def call(String message, String color = 'green') {
    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
        def colorCode = ''
        switch (color.toLowerCase()) {
            case 'red': colorCode = '\u001B[31m'; break
            case 'green': colorCode = '\u001B[32m'; break
            case 'yellow': colorCode = '\u001B[33m'; break
            case 'blue': colorCode = '\u001B[34m'; break
            default: colorCode = '\u001B[0m'
        }
        // 使用 Groovy 的 String 转义处理特殊字符
        def safeMessage = message.replaceAll(/[\u001B]/, '\\\\u001B') // 防止意外干扰
        echo "${colorCode}${safeMessage}\u001B[0m"
    }
}