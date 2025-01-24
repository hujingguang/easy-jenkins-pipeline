def call(Map params = [:]){
        withEnv([
           "BYD_PROJECT=${params.PROJECT}",
           "BYD_ENV=${params.ENV_TYPE}",
           "IMG_BASE=${params.IMG_BASE}"
         ]){
      sh '''
         if [ x"$BYD_PROJECT" == xnull ];then
         BYD_PROJECT=`echo $JOB_NAME|awk -F'-' 'OFS="-" {$NF=""; sub(/-$/, ""); print}'`
         fi
         if [ x"$BYD_ENV" == xnull ];then
         BYD_ENV=`echo $JOB_NAME |awk -F '/' '{print $1}'|awk -F '-' '{print $NF}'`
         fi
         if [ x"$IMG_BASE" == xnull ];then
         IMG_BASE=docker.xxx.com/msp/nginx:1.24.0-exporter
         fi
         if [ x"$BYD_PROJECT" == x ] || [ x"$BYD_ENV" == x ];then
              echo "无法获取项目名 或 环境类型，请检查"
              exit 1
         fi
         if [ -e ./dist/.COMMIT-VERSION ];then
            SVC_COMMIT=`cat ./dist/.COMMIT-VERSION`
            if [ -z "$SVC_COMMIT" ];then
              echo "版本信息为空，退出发布" && exit 1
            fi
         else
            echo "不存在版本信息文件，退出发布" && exit 1
         fi
         echo "BYD_PROJECT: $BYD_PROJECT BYD_ENV: $BYD_ENV BYD_SERVICE: $BYD_SERVICE"
         echo "版本：$SVC_COMMIT"
         echo "FROM $IMG_BASE" >> ./Dockerfile
         echo "COPY ./dist/ /usr/share/nginx/html/" >> ./Dockerfile
         echo "EXPOSE 80" >> ./Dockerfile
         DATETIME=`date +"%Y%m%d%H%M%S"`
         IMG_URL=docker.xxx.com/$BYD_PROJECT/$JOB_BASE_NAME:$BYD_ENV-$SVC_COMMIT.$DATETIME
         echo "当前生成的镜像构建地址： $IMG_URL"
         echo -e "\033[32m当前生成的镜像构建地址： $IMG_URL\033[0m"
         docker build -f ./Dockerfile -t $IMG_URL .
         echo "开始推送镜像"
         docker push $IMG_URL
         echo "$IMG_URL" > ./image_url.output
      '''
      def currentImage = sh(script: "cat ./image_url.output", returnStdout: true).trim()
      stash name: 'image-url', includes: 'image_url.output'
      return currentImage
      }
}
