def call(Map params = [:]){
        if (!params.repoUrl) {
           error "Parameter 'repoUrl' is required."
          }
         if (params.clearCache) {
             sh "echo 清空目录再编译"
                cleanWs()
         }else{
             sh "echo 只保留node_modules编译"
         }
         unstash 'source-code'
         sh 'ls -l'
        withEnv([
           "REPO=${params.repoUrl}",
           "BUILD=${params.buildAction}",
           "NODE=${params.nodeVersion}"
         ]){
         sh '''
             if [ x"$BUILD" == xnull ];then
             echo "没有指定编译参数，使用 yarn build " && BUILD=build
             fi
             if [ x"$NODE" == xnull ];then
             echo "没有指定node版本，使用 node18" && NODE=node18
             fi
             NODE_VERSION=$NODE
             WORKDIR=/opt/$NODE_VERSION
             PATH=$WORKDIR/bin:$PATH
             NODE_HOME=$WORKDIR
             export PATH NODE_HOME
             npm config set cache /opt/.npm_cache_$NODE_VERSION
             npm install -g yarn -registry=$REPO
             yarn config set cache-folder /opt/.yarn_cache_$NODE_VERSION
             yarn config set registry $REPO
             #echo "$REPO"|grep 'byd.com' &>/dev/null
             #if [ x"$?" == x0 ];then
             yarn config set @msp:registry=http://hub.byd.com:9081/repository/npm-soft/
             #fi
             rm -rf package-lock.json
             yarn add core-js@3.40.0 1>/dev/null
             yarn add terser@5.20.0  1>/dev/null
             yarn add spark-md5@3.0.2 1>/dev/null
             yarn install --registry=$REPO
             echo "构建命令： yarn $BUILD"
             yarn $BUILD
             cat .COMMIT-VERSION > ./dist/.COMMIT-VERSION
          '''
          }
}
