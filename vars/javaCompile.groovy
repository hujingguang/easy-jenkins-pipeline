def call(Map params = [:]){
         if (params.clearCache) {
             sh "echo 清空目录再编译"
                cleanWs()
         }
         if (!params.subPath){
             error "请传递subPath变量"
         }
         unstash 'source-code'
        withEnv([
           "MAVEN_VERSION=${params.mvnVersion}",
           "SUB_PATH=${params.subPath}",
           "IN_BUILD=${params.inBuild}"
         ]){
         echoColor('编译命令: mvn clean package -U -Dmaven.test.skip=true -s ./settings.xml -Dmaven.repo.local=/root/.mvnrepo', 'red')
         wrapColor {
         sh '''
             set +x
             echo -e "\033[32m******************************************开始编译**********************************************\033[0m"
             ls -l
             hub_setting_xml="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNldHRpbmdzIHhtbG5zPSJodHRwOi8vbWF2ZW4uYXBhY2hlLm9yZy9TRVRUSU5HUy8xLjAuMCIKICAgICAgICAgIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiCiAgICAgICAgICB4c2k6c2NoZW1hTG9jYXRpb249Imh0dHA6Ly9tYXZlbi5hcGFjaGUub3JnL1NFVFRJTkdTLzEuMC4wIGh0dHA6Ly9tYXZlbi5hcGFjaGUub3JnL3hzZC9zZXR0aW5ncy0xLjAuMC54c2QiPgogICAgPGxvY2FsUmVwb3NpdG9yeT4uLzwvbG9jYWxSZXBvc2l0b3J5PgogICAgPHBsdWdpbkdyb3Vwcz4KICAgIDwvcGx1Z2luR3JvdXBzPgogICAgPHByb3hpZXM+CiAgICA8L3Byb3hpZXM+CiAgICA8c2VydmVycz4KICAgICAgICA8c2VydmVyPgogICAgICAgICAgICA8aWQ+bXNwLXJlbGVhc2U8L2lkPgogICAgICAgICAgICA8dXNlcm5hbWU+YWRtaW4xMjM8L3VzZXJuYW1lPgogICAgICAgICAgICA8cGFzc3dvcmQ+MnZxd0B5N2w1PC9wYXNzd29yZD4KICAgICAgICA8L3NlcnZlcj4KICAgICAgICA8c2VydmVyPgogICAgICAgICAgICA8aWQ+bXNwLXNuYXBzaG90czwvaWQ+CiAgICAgICAgICAgIDx1c2VybmFtZT5hZG1pbjEyMzwvdXNlcm5hbWU+CiAgICAgICAgICAgIDxwYXNzd29yZD4ydnF3QHk3bDU8L3Bhc3N3b3JkPgogICAgICAgIDwvc2VydmVyPgogICAgPC9zZXJ2ZXJzPgoKICAgIDxtaXJyb3JzPgogICAgICAgIDxtaXJyb3I+CiAgICAgICAgICAgIDxpZD5uZXh1czwvaWQ+CiAgICAgICAgICAgIDxuYW1lPmJ5ZCBtYXZlbjwvbmFtZT4KICAgICAgICAgICAgPHVybD5odHRwOi8vaHViLmJ5ZC5jb206OTA4MS9yZXBvc2l0b3J5L21hdmVuLWdyb3VwLzwvdXJsPgogICAgICAgICAgICA8bWlycm9yT2Y+KjwvbWlycm9yT2Y+CiAgICAgICAgPC9taXJyb3I+CiAgICA8L21pcnJvcnM+CgogICAgPHByb2ZpbGVzPgogICAgICAgIDxwcm9maWxlPgogICAgICAgICAgICA8aWQ+YnlkLWdyb3VwPC9pZD4KICAgICAgICAgICAgPGFjdGl2YXRpb24+CiAgICAgICAgICAgICAgICA8YWN0aXZlQnlEZWZhdWx0PnRydWU8L2FjdGl2ZUJ5RGVmYXVsdD4KICAgICAgICAgICAgPC9hY3RpdmF0aW9uPgogICAgICAgICAgICA8cmVwb3NpdG9yaWVzPgogICAgICAgICAgICAgICAgPHJlcG9zaXRvcnk+CiAgICAgICAgICAgICAgICAgICAgPGlkPm1hdmVuLWdyb3VwPC9pZD4KICAgICAgICAgICAgICAgICAgICA8bmFtZT5tYXZlbiBncm91cDwvbmFtZT4KICAgICAgICAgICAgICAgICAgICA8cmVsZWFzZXM+CiAgICAgICAgICAgICAgICAgICAgICAgIDxlbmFibGVkPnRydWU8L2VuYWJsZWQ+CiAgICAgICAgICAgICAgICAgICAgPC9yZWxlYXNlcz4KICAgICAgICAgICAgICAgICAgICA8c25hcHNob3RzPgogICAgICAgICAgICAgICAgICAgICAgICA8ZW5hYmxlZD50cnVlPC9lbmFibGVkPgogICAgICAgICAgICAgICAgICAgIDwvc25hcHNob3RzPgogICAgICAgICAgICAgICAgICAgIDx1cmw+aHR0cDovL2h1Yi5ieWQuY29tOjkwODEvcmVwb3NpdG9yeS9tYXZlbi1ncm91cC88L3VybD4KICAgICAgICAgICAgICAgICAgICA8bGF5b3V0PmRlZmF1bHQ8L2xheW91dD4KICAgICAgICAgICAgICAgIDwvcmVwb3NpdG9yeT4KICAgICAgICAgICAgPC9yZXBvc2l0b3JpZXM+CiAgICAgICAgICAgIDxwcm9wZXJ0aWVzPgogICAgICAgICAgICAgICAgPHByb2plY3QuYnVpbGQuc291cmNlRW5jb2Rpbmc+VVRGLTg8L3Byb2plY3QuYnVpbGQuc291cmNlRW5jb2Rpbmc+CiAgICAgICAgICAgICAgICA8cHJvamVjdC5yZXBvcnRpbmcub3V0cHV0RW5jb2Rpbmc+VVRGLTg8L3Byb2plY3QucmVwb3J0aW5nLm91dHB1dEVuY29kaW5nPgogICAgICAgICAgICA8L3Byb3BlcnRpZXM+CiAgICAgICAgICAgIDxwbHVnaW5SZXBvc2l0b3JpZXM+CiAgICAgICAgICAgICAgICA8cGx1Z2luUmVwb3NpdG9yeT4KICAgICAgICAgICAgICAgICAgICA8aWQ+bWF2ZW4tZ3JvdXA8L2lkPgogICAgICAgICAgICAgICAgICAgIDxuYW1lPm1hdmVuIGdyb3VwPC9uYW1lPgogICAgICAgICAgICAgICAgICAgIDxyZWxlYXNlcz4KICAgICAgICAgICAgICAgICAgICAgICAgPGVuYWJsZWQ+dHJ1ZTwvZW5hYmxlZD4KICAgICAgICAgICAgICAgICAgICA8L3JlbGVhc2VzPgogICAgICAgICAgICAgICAgICAgIDxzbmFwc2hvdHM+CiAgICAgICAgICAgICAgICAgICAgICAgIDxlbmFibGVkPnRydWU8L2VuYWJsZWQ+CiAgICAgICAgICAgICAgICAgICAgPC9zbmFwc2hvdHM+CiAgICAgICAgICAgICAgICAgICAgPHVybD5odHRwOi8vaHViLmJ5ZC5jb206OTA4MS9yZXBvc2l0b3J5L21hdmVuLWdyb3VwLzwvdXJsPgogICAgICAgICAgICAgICAgPC9wbHVnaW5SZXBvc2l0b3J5PgogICAgICAgICAgICA8L3BsdWdpblJlcG9zaXRvcmllcz4KICAgICAgICA8L3Byb2ZpbGU+CiAgICA8L3Byb2ZpbGVzPgo8L3NldHRpbmdzPgo="
             WORKDIR=/opt/$MAVEN_VERSION
             PATH=$WORKDIR/bin:/opt/jdk8/bin:$PATH
             CURRENT_DIR=`pwd`
             export PATH
             if [ xnull == x$SUB_PATH ];then
                 SUB_PATH='./'
             fi
             echo "IN_BUILD: $IN_BUILD"
             if [ x0 == x$IN_BUILD ];then
                 echo "不进入目录编译"
                 echo ${hub_setting_xml}|base64 -d >./settings.xml
             else
                 echo "进入目录编译"
                 cd ${SUB_PATH} && echo ${hub_setting_xml}|base64 -d >./settings.xml
             fi

             #set -x
             echo "当前目录: `pwd`"
             echo "构建命令： $WORKDIR/bin/mvn clean package -U -Dmaven.test.skip=true -s ./settings.xml -Dmaven.repo.local=/root/.mvnrepo"
             $WORKDIR/bin/mvn clean package -U -Dmaven.test.skip=true -s ./settings.xml -Dmaven.repo.local=/root/.mvnrepo

             #if [ xnull == x$IN_BUILD ];then
             #    num=`find ./target/ -maxdepth 1 -name '*.jar' |wc -l`
             #else
             #    num=`find $SUB_PATH/target/ -maxdepth 1 -name '*.jar' |wc -l`
             #fi

             num=`find $CURRENT_DIR/$SUB_PATH/target/ -maxdepth 1 -name '*.jar' |wc -l`

             if [ x"$num" != x1 ];then
                echo "生成的Jar不存在或者有多个，无法进行发布,请检查代码"
                ls -l $CURRENT_DIR/$SUB_PATH/target
                exit 1
             fi
             if [ x0 == x$IN_BUILD ];then
                cat $CURRENT_DIR/.COMMIT-VERSION > $SUB_PATH/target/COMMIT-VERSION.jar
             else
                cat $CURRENT_DIR/.COMMIT-VERSION > ./target/COMMIT-VERSION.jar
             fi
          '''
          }
          stash name: 'jarBar', includes: "${params.subPath}/target/*.jar"
          }
}
