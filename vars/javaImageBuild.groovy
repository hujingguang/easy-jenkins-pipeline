def call(Map params = [:]){

        withEnv([
           "BYD_PROJECT=${params.PROJECT}",
           "BYD_ENV=${params.ENV_TYPE}",
           "IMG_BASE=${params.IMG_BASE}",
           "PORT=${params.PORT}",
           "TYPE=${params.DOCKERFILE}"
         ]){
      sh '''
         if [ x"$BYD_PROJECT" == xnull ];then
         BYD_PROJECT=`echo $JOB_NAME|awk -F'-' 'OFS="-" {$NF=""; sub(/-$/, ""); print}'`
         fi
         if [ x"$BYD_ENV" == xnull ];then
         BYD_ENV=`echo $JOB_NAME |awk -F '/' '{print $1}'|awk -F '-' '{print $NF}'`
         fi
         if [ x"$PORT" == xnull ];then
            PORT=8080
         fi
         if [ x"$TYPE" == xnull ];then
            echo "未指定Dockerfile文件类型，使用默认值"
            TYPE=1
         fi
         if [ x"$IMG_BASE" == xnull ];then
         IMG_BASE=hub.byd.com/pub-image/apache/skywalking-base:9.2
         fi
         if [ x"$BYD_PROJECT" == x ] || [ x"$BYD_ENV" == x ];then
              echo "无法获取项目名 或 环境类型，请检查"
              exit 1
         fi
         commitFile=`find . -name '*.jar'|grep  'COMMIT'`
         if [ -e $commitFile ];then
            SVC_COMMIT=`cat $commitFile`
            if [ -z "$SVC_COMMIT" ];then
              echo "版本信息文件为空，退出发布" && exit 1
            fi
         else
            echo "不存在版本信息文件，退出发布" && exit 1
         fi
         echo "BYD_PROJECT: $BYD_PROJECT BYD_ENV: $BYD_ENV BYD_SERVICE: $BYD_SERVICE"
         echo "版本：$SVC_COMMIT"
         jarName=`find ./ -name '*.jar' 2>/dev/null |grep -v COMMIT|awk -F'/' '{print $NF}'`
         jarFile=`find . -name '*.jar' |grep -v 'COMMIT'`
         jvmArgs=' -Duser.timezone=GMT+08 -XX:+UseContainerSupport -XX:InitialRAMPercentage=70.0 -XX:MaxRAMPercentage=70.0 -Dspring.profiles.active=${active} -Dfile.encoding=utf-8 '
         #老工程Dockerfile文件
cat << EOF > 1.Dockerfile
         FROM $IMG_BASE
         COPY $jarFile /opt/$jarName
         EXPOSE $PORT
         CMD dumb-init java $jvmArgs -javaagent:/usr/local/skywalking-agent/skywalking-agent.jar -jar /opt/${jarName}  --nacos.host=\\${NACOS_ADDR} --nacos.password=\\${NACOS_PASSWORD} --nacos.username=\\${NACOS_USER} --nacos.namespace=\\${NACOS_NAMESPACE}
EOF
cat <<EOF > 2.Dockerfile
         FROM $IMG_BASE
         VOLUME /tmp
         ADD $jarFile /app.jar
EOF

cat <<EOF > 3.Dockerfile
         FROM $IMG_BASE
         VOLUME /tmp
         ADD $jarFile /opt/app.jar
EOF

         echo "写入1.Dockerfile"
         DATETIME=`date +"%Y%m%d%H%M%S"`
         IMG_URL=docker.byd.com/$BYD_PROJECT/$JOB_BASE_NAME:$BYD_ENV-$SVC_COMMIT.$DATETIME
         echo "当前生成的镜像构建地址： $IMG_URL"
         echo -e "\033[32m当前生成的镜像构建地址： $IMG_URL\033[0m"
         echo "当前选择的dockerfile文件: $TYPE.Dockerfile"
         docker build -f ./$TYPE.Dockerfile -t $IMG_URL .
         docker push $IMG_URL
         echo "$IMG_URL" > ./image_url.output
      '''
       def currentImage = sh(script: "cat ./image_url.output", returnStdout: true).trim()
       stash name: 'image-url', includes: 'image_url.output'
       return currentImage
      }
}
