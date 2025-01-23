def call(Map params = [:]){
   if (!params.containsKey('NAMESPACE') || params.NAMESPACE == null) {
        error "参数 'NAMESPACE' 未提供或为空！"
    }
   if (!params.containsKey('SERVICE_NAME') || params.SERVICE_NAME == null) {
        error "参数 'SERVICE_NAME' 未提供或为空！"
    }
   if (!params.containsKey('CLUSTER') || params.CLUSTER == null) {
        error "参数 'CLUSTER' 未提供或为空！"
    }
   if(!params.containsKey('UPDATE_IMAGE') || params.UPDATE_IMAGE == null){
      unstash 'image-url'
   }else{
      echo "直接更新镜像"
   }
   withCredentials([
   string(credentialsId: 'Hu_k8s_config_uat', variable: 'Hu_uat'),
   string(credentialsId: 'Hu_k8s_config_prod', variable: 'Hu_prod')
   ]) {
   withEnv([
           "NAMESPACE=${params.NAMESPACE}",
           "CLUSTER=${params.CLUSTER}",
           "SERVICE_NAME=${params.SERVICE_NAME}",
           "UPDATE_IMAGE=${params.UPDATE_IMAGE}"
         ]){
           sh '''
           if [ -e ./image_url.output ];then
           updateImage=`cat ./image_url.output`
           else
           updateImage=$UPDATE_IMAGE
           echo "从参数中获取变量进行更新"
           fi
           set +x
           USER_VAR=$CLUSTER
           declare -n user_conf=$USER_VAR
           if [ x$user_conf == x ];then
           echo "无法匹配 kubeconf 文件，请检查参数是否正确" && exit 1
           fi
           echo $user_conf|base64 -d > ./.kube.conf
           echo "开始更新应用"
           /bin/kubectl --kubeconfig ./.kube.conf set image deployment/$SERVICE_NAME $SERVICE_NAME=$updateImage -n $NAMESPACE
           rm -f ./.kube.conf
           set -x
           '''
     }
   }

}
