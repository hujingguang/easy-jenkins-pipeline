#!/bin/env python3
import os
import yaml
import optparse
import logging
import sys
import pprint
import copy
import jenkins
from api4jenkins import Jenkins
import time

# 关闭 api4jenkins 的日志
logging.getLogger("api4jenkins").setLevel(logging.CRITICAL)
logging.getLogger("urllib3").setLevel(logging.CRITICAL)

def initLoger():
    import colorlog
    logger = colorlog.getLogger()
    logger.setLevel(logging.WARNING)
    console_handler = colorlog.StreamHandler()
    console_handler.setLevel(logging.INFO)
    formatter = colorlog.ColoredFormatter(
        '%(log_color)s%(asctime)s - %(levelname)s - %(funcName)s - Line: %(lineno)d  - %(message)s',
        log_colors={
            'DEBUG': 'cyan',
            'INFO': 'green',
            'WARNING': 'green',
            'ERROR': 'red',
            'CRITICAL': 'red,bg_white',
        }
    )
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    return logger


logger = initLoger()

api_conn = None

def jenkins_login(url, user, password):
    conn = None
    try:
        conn = jenkins.Jenkins(url, username=user, password=password)
    except Exception as e:
        logger.error("Jenkins登录失败，请检查相应参数是否正确")
        logger.error(e)
    return conn

def jenkins_login_by_api(url,user,password):
    conn = None
    try:
        conn = Jenkins(url, auth=(user, password))
    except Exception as e:
        logger.error("Jenkins登录失败，请检查相应参数是否正确")
        logger.error(e)
    return conn



def is_exsit(conn,jobPath):
    try:
         job_data = conn.get_job_info(jobPath)
         sub_jobs = []
         for job in job_data['jobs']:
             sub_jobs.append(job)
    except Exception as e:
        logger.warning("JOB: {0} 不存在,自动进行创建".format(jobPath))
        return False,[]
    return True,sub_jobs


def create_dir_job(conn,jobPath,displayName):
    try:
        #conn.create_folder(jobPath)
        folder_config = f"""<?xml version='1.1' encoding='UTF-8'?>
        <com.cloudbees.hudson.plugins.folder.Folder plugin="cloudbees-folder">
          <actions/>
          <description></description>
          <properties/>
          <folderViews/>
          <healthMetrics/>
          <icon class="com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon"/>
          <displayName>{displayName}</displayName>
        </com.cloudbees.hudson.plugins.folder.Folder>
        """
        api_conn.create_job(jobPath, folder_config)
        logger.warning("目录Job: {0} 已创建".format(jobPath))
        #info  = conn.get_job_info(jobPath)
        #current_job = api_conn[jobPath]
        logger.warning("目录Job: {0} 修改显示名成功".format(jobPath))
    except Exception as e:
        logger.error(e)
        return False
    return True


def create_pipeline_job(conn,service,config,refresh=False):
    if config['lightCheckout']:
        lightCheckout = 'true'
    else:
        lightCheckout = 'false'
    pipeline_config = f"""
    <flow-definition plugin="workflow-job@2.40">
      <actions/>
      <description>A pipeline job using script from SCM</description>
      <keepDependencies>false</keepDependencies>
      <properties/>
      <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.94">
        <scm class="hudson.plugins.git.GitSCM" plugin="git@4.10.0">
          <configVersion>2</configVersion>
          <userRemoteConfigs>
            <hudson.plugins.git.UserRemoteConfig>
              <url>{config['scmRepo']}</url>
              <credentialsId>{config['scmCredentials']}</credentialsId>
            </hudson.plugins.git.UserRemoteConfig>
          </userRemoteConfigs>
          <branches>
            <hudson.plugins.git.BranchSpec>
              <name>{config['scmBranch']}</name>
            </hudson.plugins.git.BranchSpec>
          </branches>
          <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
          <submoduleCfg class="list"/>
          <extensions/>
        </scm>
        <scriptPath>{config['scriptPath']}</scriptPath>
        <lightweight>{lightCheckout}</lightweight>
      </definition>
      <disabled>false</disabled>
    </flow-definition>
    """
    try:
        if refresh:
            #job = conn[service]
            conn.reconfig_job(service,pipeline_config)
        else:
            conn.create_job(service, pipeline_config)
    except Exception as e:
        logger.error(e)
        return False
    return True

def trigger_job(conn,service):
    try:
        logger.warning("开始第一次触发Job: {0} 初始化git仓库以便获取分支参数信息".format(service))
        job = conn[service]
        job.build()
    except Exception as e:
        logger.error(e)
        logger.error("触发失败")


def parse_job_data(data):
    global api_conn
    try:
        for ns,info in data.items():
            if ns == 'global':
                continue
            logger.info("开始处理命名空间: {0} 的job 数据".format(ns))
            jenkins_info = info['jenkinsInfo']
            conn = jenkins_login(jenkins_info['jenkinsServer'],jenkins_info['jenkinsUser'], jenkins_info['jenkinsToken'])
            api_conn = jenkins_login_by_api(jenkins_info['jenkinsServer'],jenkins_info['jenkinsUser'], jenkins_info['jenkinsToken'])
            if not conn or not api_conn:
                return
            job_info = info['jobInfo']
            job_path = job_info['jobPath']
            currentPath=job_path['jobName']
            jobs_mapping = dict()
            while True:
                displayName = job_path['jobDisplay']
                result,sub_jobs = is_exsit(conn, currentPath)
                jobs_mapping[currentPath] = sub_jobs
                if not result:
                    if not create_dir_job(conn,currentPath,displayName):
                        sys.exit(1)
                if 'childPath' in job_path and isinstance(job_path['childPath'],dict):
                    if 'jobDisplay' in job_path['childPath'] and 'jobName' in job_path['childPath']:
                        job_path = job_path['childPath']
                        currentPath = currentPath.lstrip('/').rstrip('/') + '/' + job_path['jobName'].replace('/','')
                    else:
                        logger.error("数据格式有误，请检查 {0} 字段数据".format(currentPath))
                        sys.exit(1)
                else:
                    break

            job_list = [job['name'] for job in sub_jobs]
            for service in info['services']:
                config = {
                    "scmRepo": job_info['scmRepo'],
                    "scmCredentials": job_info['scmCredentials'],
                    "scmBranch": job_info['scmBranch'],
                    "lightCheckout": job_info['lightCheckout'],
                    "scriptPath": job_info['scriptPath'] + '/' + service + '.jenkinsfile'
                }
                if service not in job_list:
                    logger.warning("开始创建 流水线 Job: {0}".format(service))
                    success_info = "流水线 Job: {0} 创建成功".format(service)
                    failed_info = "流水线 Job: {0} 创建失败".format(service)
                    service = currentPath + '/' + service.replace('/','')
                    # 使用api4jenkins模块创建
                    if create_pipeline_job(api_conn,service,config):
                        logger.warning(success_info)
                        if 'firstTrigger' in job_info and job_info['firstTrigger']:
                            trigger_job(api_conn, service)
                    else:
                        logger.error(failed_info)
                        sys.exit(1)
                else:
                    if 'refresh' in job_info and job_info['refresh']:
                        service = currentPath + '/' + service.replace('/', '')
                        logger.warning("开始更新流水线 Job: {0} 配置".format(service))
                        #使用python-jenkins模块更新
                        create_pipeline_job(conn,service,config,refresh=True)
                    else:
                        logger.warning("流水线 Job： {0} 已存在，跳过".format(service))

    except Exception as e:
        logger.error(e)
        sys.exit(1)






def parse_yaml(path):
    data = None
    try:
        with open(path, "r",encoding="utf-8") as file:
            data = yaml.safe_load(file)
    except Exception as e:
        logger.error(e)
    return data





def parse_yarm_data(data):
    keys = ['namespaces', 'global']
    result = {}
    for key in keys:
        if key not in data:
            logger.error("Yaml文件缺少 {0} 字段".format(key))
            sys.exit(1)
    global_data = None
    if 'global' in data and data['global']:
        global_data = data['global']
    result['global'] = copy.deepcopy(global_data)

    if 'namespaces' in data and isinstance(data['namespaces'], list):
        for namespace in data['namespaces']:
            if 'namespace' not in namespace or not namespace['namespace']:
                logger.error("命名空间缺失 name 字段")
                sys.exit(1)
            if namespace['namespace'] not in result:
                result[namespace['namespace']] = {}
                result[namespace['namespace']]['services'] = []

            if 'jobInfo' in namespace and isinstance(namespace['jobInfo'], dict):
                result[namespace['namespace']]['jobInfo'] = namespace['jobInfo']
            else:
                logger.error("命名空间: {0} 缺少 jobInfo 字段，无法创建jenkins job".format(namespace['namespace']))
                sys.exit(1)

            if 'jenkinsInfo' in namespace and isinstance(namespace['jenkinsInfo'], dict):
                result[namespace['namespace']]['jenkinsInfo'] = namespace['jenkinsInfo']
            else:
                logger.error("命名空间: {0} 缺少 jenkinsInfo 字段".format(namespace['namespace']))
                sys.exit(1)

            if 'projects' not in namespace or not isinstance(namespace['projects'], list):
                logger.error("命名空间: {0} 缺失 projects 字段".format(namespace['namespace']))
                sys.exit(1)


            for project in namespace['projects']:
                if "service" in project and project['service'] and isinstance(project['service'], str):
                    result[namespace['namespace']]["services"].append(project['service'])
    return result








def parse_option():
    option_parser = optparse.OptionParser(usage="Jenkins工程自动化创建工具")
    option_parser.add_option('-i', '--input-yaml-path', action='store', type='string', help="工程配置文件",
                             dest="configFile")
    options, args = option_parser.parse_args()
    configFile = options.configFile
    if not configFile:
        logger.error('缺少配置文件,请使用 -i  指定')
        option_parser.print_help()
        sys.exit(1)
    if  not os.path.exists(configFile):
        logger.error("配置文件不存在， 请检查")
        option_parser.print_help()
        sys.exit(1)
    data = parse_yarm_data(parse_yaml(configFile))
    parse_job_data(data)



parse_option()