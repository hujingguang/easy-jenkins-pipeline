

def call(String namespace, String service, String timeout,String config, String updateImage){

   withCredentials([
       string(credentialsId: 'Hu_k8s_config_uat', variable: 'Hu_uat'),
       string(credentialsId: 'Hu_k8s_config_prod', variable: 'Hu_prod')
   ]) {

   withEnv([
           "NAMESPACE=${namespace}",
           "CLUSTER=$config",
           "SERVICE_NAME=${service}",
           "TIMEOUT=$timeout",
           "CURRENT_IMAGE=$updateImage"
         ]){
           echo "命名空间: $NAMESPACE 服务名: $SERVICE_NAME 镜像地址: $CURRENT_IMAGE"
           sh '''
           set +x
           USER_VAR=$CLUSTER
           declare -n user_conf=$USER_VAR
           if [ x$user_conf == x ];then
           echo "无法匹配 kubeconf 文件，请检查参数是否正确" && exit 1
           fi
           echo $user_conf|base64 -d > ./.kube.conf
           '''
      }
   }
           def image = sh(script: "/bin/kubectl --kubeconfig ./.kube.conf describe deployments.apps ${service}   -n ${namespace}  |grep 'Image:'|grep '${namespace}'|grep '${service}'|awk '{print \$NF}' || true ", returnStdout: true).trim()
           echo "当前的运行镜像: $image"
           def ok = 0
           if(image==updateImage){
              echo "镜像对比成功，开始检测应用是否就绪 "
              def result = sh(script: "/bin/kubectl --kubeconfig ./.kube.conf rollout status deployments.apps ${service}   -n ${namespace} --timeout ${timeout}s  |grep 'successfully' || true ", returnStdout: true).trim()
              if(result.length()>10){
                   echoColor('应用启动成功')
                   ok = 1
               }
           }
          
          sh 'rm -f ./.kube.conf'
          if(ok==0){
               echoColor('应用启动失败',"red")
               error "退出发布"
          }

}
