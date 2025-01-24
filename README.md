# 代码库说明

## 介绍

jenkins CI/CD 框架 ，pipeline as code ，将服务信息通过YAML配置后，使用jinja2 批量渲染出jenkinsfile 通过jenkins API自动化创建job 轻松管理和更新成百上千的服务

## 目录说明

configure: 该目录存放各个环境对应服务的自定义参数，用来生成服务的jenkinsfile

project: 该目录为工程实际的jenkinsfile文件，jenkins从该目录获取对应服务的具体流水线文件进行构建

resources: 资源文件，目前主要存放了生产环境发布对应服务的发布审核人员信息

templates: jenkinsfile j2 模板文件，用来渲染服务的实际jenkinsfile

vars: jenkins共享库方法的具体实现，文件名对应调用的函数名

## 使用说明


./update.sh 批量更新jenkinsfile

./render_jenkinsfile.py jenkinsfile渲染工具 linux的二进制版本

./jenkins_tool.py 自动化创建工程并触发第一次构建工具




## 使用范例

1. 新上线一个新的项目,名字为 demo，里面有如下服务 test1 test2

2. 进入 configure 目录，再进入 dev 目录 拷贝一个现有的配置 例如 ipmp.yaml文件 重命名为 demo.yaml

3. 配置说明

```yaml
global:
  nodeLabel:
    checkoutLabel: "master"  #代码检出执行的slave 标签节点   
    compileLabel: "k8s-slave01"
    buildLabel: "slave01"
    updateLabel: "slave01"
    healCheckLabel: "k8s-slave02"
  npmRepo:
    internal: "http://hub.xxx.com:9081/repository/npm-group" #前端工程编译内部npm源
    external: "https://registry.npmmirror.com"            #前端工程外部npm源 ，先选择外部源如果编译失败，会选择内部源进行第二次编译重试

namespaces:
  - namespace: demo-dev  #k8s命名空间名，项目名为demo 开发环境为dev 则写为 demo-dev
    dirPath: project/demo/dev        #渲染模版后生成的流水线目录位置
    authId: "gitUser"                #拉取服务git仓库代码的用户名密码认证id ,再jenkins凭据管理配置后填写
    envType: dev                     #环境类型
    k8sConf: "Hu_uat"                #k8s config文件的base64编码 存储到jenkins凭证管理 类型为 secret text的 id 值

    jenkinsInfo:                     #jenkins自动创建工程信息配置
      jenkinsServer: http://10.28.20.11:32759 #jenkins系统地址
      jenkinsUser: YOU User Name              #jenkins登录用户名
      jenkinsToken: API Token                 #用户API Token
    jobInfo:
      scmRepo: https://devops.xxx.com/zonghefuwuxinxibu//jenkins-auto.git  #jenkinsfile文件的仓库地址
      refresh: false                                                                   #如果工程已经在jenkins上创建，是否更新工程配置信息
      firstTrigger: true                                                               #创建jenkins工程后是否第一次自动触发,可以避免首次发布无法获取分支信息
      scmCredentials: "devops-gitee"                                                   #jenkinsfile文件代码仓库认证的id ,提前在jenkins上配置
      scmBranch: '*/master'                                                            #拉取jenkinsfile时默认使用的分支
      scriptPath: project/mdm-pmp/dev                                                  #jenkinsfile具体的路径
      lightCheckout: true                                                              #是否使用轻量级拉取，只拉取使用的jenkinsfile文件
      jobPath:
        jobName: demo                                                                  #在jenkins上创建的文件夹，不存在会自动创建
        jobDisplay: "范例项目-demo"                                                     #文件显示名
        childPath:                                                                     #子目录信息
          jobName: dev
          jobDisplay: "开发环境"                                                       #子目录显示名，如果还有子目录按照目录结构进行继续配置,创建后projects字段下的所有service会自动归档在改目录下

    projects:
      - service: test1               #服务名 web工程类型
        template: templates/dev/public-web.j2  #用来渲染服务的模版位置
        params:
          gitAddr: http://code.xxx.com.cn/IPMP/IPMP-WEB.git # test1服务的代码地址
          nodeVersion: node18                               # 前端工程的参数，node的版本，可选值为： node14 node16 node18 node20
          dockerFrom: docker.xxx.com/msp/nginx:1.24.0-exporter # 前端工程Dockerfile的基础镜像地址
          buildAction: "build"                                 # npm run 的参数，如果为 build  编译命令为：npm run build

      - service: test2               #服务名 java工程类型
        template: templates/dev/public-java.j2  
        params:
          gitAddr: http://code.xxx.com.cn/IPMP/IPMP.git
          mvnVersion: maven396                                #java编译使用maven工具,版本为 3.9.6
          subPath: byd-module-ipmp/byd-module-ipmp-biz        #编译子目录相对位置，jar会在该目录下的target目录下生成
          inBuild: 0                                          #是否进入子目录里面编译，0表示不进入，1表示进入
          port: 8099                                          #服务监听端口
          dockerFile: 2                                       #Dockerfile类型，可选值为1,2,3 如果自己的Dockerfile不同，在 vars目录下的 javaImageBuild.groovy文件中配置
          dockerFrom: "docker.xxx.com/msp/openjdk:8u352b08-jmx-skywalking"       #Dockerfile 基础镜像



```
4. 配置好开发环境的服务信息后，执行 ./render_jenkinsfile -i ./configure/dev/demo.yaml 进行jenkinsfile渲染 


5. 渲染完成后提交代码： git add -A . && git commit -m 'update' && git push origin master 提交代码


6. 执行 ./jenkins_tool.py -i ./configure/dev/demo.yaml 进行jenkins工程自动创建



# Jenkins Server安装


1. Jenkins服务需要安装的插件如下

```
LDAP #插件

Login Page Theme #登录页自定义插件

Parameter #流水线分支选择插件

Pipeline Stage View #流水线stage视图显示

Dashboard View  #doashboard 视图插件

Distributed Workspace Clean #工作区清理插件

Role-based Authorization Strategy #基于角色的权限管理插件

AnsiColor # echo 颜色插件

Material Theme #皮肤插件

Active Choices #联动参数插件


build user vars #在系统配置中打开Enabled for all builds 就可以通过env.BUILD_USER获取构建用户

Build Authorization Token Root # 直接token触发构建插件无需用户名，无需配置，区别于jenkins自己的API构建配置

Performance  #jmeter性能测试结果显示插件

Build Monitor #构建状态监控大盘插件

Parameterized Remote Trigger#在流水线中触发另外的job插件，使用文档 https://github.com/jenkinsci/parameterized-remote-trigger-plugin/blob/master/README_PipelineConfiguration.md
```

2. Slave节点的镜像地址：  registry.cn-shenzhen.aliyuncs.com/hoover/hoover:jenkinslave2


3. 进入jekins系统配置 ---> Global Trusted Pipeline Libraries 使用当前仓库配置好git共享库


# 使用效果
1. 测试环境构建

![](https://github.com/hujingguang/easy-jenkins-pipeline/blob/main/screens/2.png)

![](https://github.com/hujingguang/easy-jenkins-pipeline/blob/main/screens/1.png)

1. 生产环境构建

![](https://github.com/hujingguang/easy-jenkins-pipeline/blob/main/screens/4.png)

![](https://github.com/hujingguang/easy-jenkins-pipeline/blob/main/screens/3.png)

