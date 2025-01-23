import groovy.json.JsonSlurper
import groovy.json.JsonException



def checkDeployTime(){
    echo "检测是否为发布窗口"
}


def deployInterrupt(int number){
      int buildNumber = env.BUILD_NUMBER.toInteger()
      if (number >= buildNumber) {
        echo "当前构建号：${env.BUILD_NUMBER}, 小于最小构建号：$number 的构建进行中断"
        error "退出发布"
      }
}


def getAuditPerson(String nameSpace, String serviceName="default"){
    try{
        def jsonContent = libraryResource('auditUsers.json')
        def auditUsers = new JsonSlurper().parseText(jsonContent)
        if(auditUsers.containsKey(nameSpace)){
            if(auditUsers[nameSpace].containsKey(serviceName)){
               def users = auditUsers[nameSpace][serviceName]
               if(users && users.size()>0){
                   def webhook = ""
                   if(auditUsers[nameSpace].containsKey("webhook")){
                       webhook = auditUsers[nameSpace].webhook
                    }
                   return [users,webhook]
               }else{
                   echoColor(" 命名空间: $nameSpace 审计人员列表为空","red")
                   error "Exited"
               }
            }
        }else{
           echoColor(" 命名空间：$nameSpace 没有在 resources/auditUsers.json 文件中配置审核人员,请检查")
           error "Exited"
        }
    }catch(JsonException e){
        echoColor("Error parsing JSON: ${e.message}","red")
        error "Exited"
    }catch(Exception e){
        // 捕获其他异常
        echoColor("Unexpected error: ${e.message}","red")
        error "Exited"
    }
}



def checkImageUrl(String image_url, boolean stickCheck = true){
    echoColor(" 检查镜像地址格式: $image_url 是否合法","blue")
    def parttern = /^(docker|hub)\.byd\.com\/(.+)\/(.+)\:(.+)/
    if(stickCheck && !image_url.contains(env.JOB_BASE_NAME)){
         error "镜像地址格式不正确，地址中没有包含服务名信息"
    }
    if(image_url =~ parttern){
       echoColor(" 镜像地址格式检测通过")
       echoColor(" 检查镜像地址是否在仓库存在: $image_url 是否合法","blue")
       imageParams = image_url.split(':')
       def imageApiBase = imageParams[0].replace('docker.byd.com',"http://docker.byd.com:9081/repository/docker-group/v2").replace('hub.byd.com',"http://hub.byd.com:9081/repository/docker-group/v2")
       def imageTag = imageParams[1]
       def imageApi = imageApiBase + '/tags/list'
       def tagList = sh(script:"curl -s  $imageApi ||true ", returnStdout: true).trim()
       if(tagList.contains(imageTag)){
           echoColor("仓库镜像地址存在，检测通过")
       }else{
           echoColor("远程仓库不存在该镜像", "red")
           error "发布退出"
       }
    }else{
       echoColor("镜像地址格式不正确，格式标准: docker.byd.com/命名空间/服务名:版本 不能包含空格!","red")
       error "发布退出"
    }

}


def call(Map Args = [:]){
    checkDeployTime()
    echo "前置检查通过，开始进行发布, 传递的参数:  ${Args.imageUrl}"
}
