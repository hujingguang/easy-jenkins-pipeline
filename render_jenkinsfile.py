#!/bin/env python3
import os
import yaml
from jinja2 import Template
import optparse
import logging
import sys
import pprint
import copy


# pip install jinja2 yaml -i https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple 安装模块

def initLoger():
    import colorlog
    logger = colorlog.getLogger()
    logger.setLevel(logging.DEBUG)
    console_handler = colorlog.StreamHandler()
    console_handler.setLevel(logging.INFO)
    formatter = colorlog.ColoredFormatter(
        '%(log_color)s%(asctime)s - %(levelname)s - %(funcName)s - Line: %(lineno)d  - %(message)s',
        log_colors={
            'DEBUG': 'cyan',
            'INFO': 'green',
            'WARNING': 'yellow',
            'ERROR': 'red',
            'CRITICAL': 'red,bg_white',
        }
    )
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    return logger


logger = initLoger()


def parse_yaml(path):
    data = None
    try:
        with open(path, "r",encoding="utf-8") as file:
            data = yaml.safe_load(file)
            #pprint.pprint(data)
    except Exception as e:
        logger.error(e)
    return data


def render_template(data):
    render_data = {}
    render_data['global'] = copy.deepcopy(data['global'])
    current_dir = os.getcwd()
    for key,info in data.items():
        if key != "global":
            logger.info("开始渲染命名空间: {0}".format(key))
            try:
                create_dir_path = os.path.join(current_dir,info['createDir'])
                if not os.path.exists(create_dir_path):
                    logger.info("开始创建目录: {0}".format(create_dir_path))
                    os.makedirs(create_dir_path)
                for service in info['services']:
                    jenkins_file = os.path.join(create_dir_path, service['service'])
                    template_file = service['template']
                    if not os.path.exists(template_file):
                        logger.error("当前目录不存在模板文件：{0}".format(template_file))
                        sys.exit(1)
                    logger.info("开始渲染服务: {0}, 路径: {1}".format(service['service'],jenkins_file))
                    with open(template_file, "r",encoding="utf-8") as _template_file:
                        template_content = _template_file.read()
                    template = Template(template_content)
                    output = template.render(service)
                    with open(jenkins_file+'.jenkinsfile','w',encoding="utf-8") as f:
                        f.write(output)
            except Exception as e:
                logger.error(e)



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

            if 'dirPath' in namespace and isinstance(namespace, dict):
                result[namespace['namespace']]['createDir'] = namespace['dirPath']
            else:
                logger.error("命名空间: {0} 缺少 dirPath 字段，无法定位当前jenkins创建路径".format(namespace['namespace']))
                sys.exit(1)
            if 'projects' not in namespace or not isinstance(namespace['projects'], list):
                logger.error("命名空间: {0} 缺失 projects 字段".format(namespace['namespace']))
                sys.exit(1)

            #获取每个工程的配置并展开
            for project in namespace['projects']:
                service_info = dict()
                if "service" in project and project['service'] and isinstance(project['service'], str):
                    if 'params' in project and isinstance(project['params'], dict):
                        service_info = copy.deepcopy(project['params'])
                    if 'template' in project and isinstance(project['template'], str):
                        service_info['template'] = project['template']
                    service_info['service'] = project['service']
                for key, value in namespace.items():
                    if key not in service_info and key not in ["projects", "dirPath"]:
                        service_info[key] = value

                if 'npmRepo' in global_data and isinstance(global_data['npmRepo'], dict):
                    if 'external' in global_data['npmRepo'] and global_data['npmRepo']['external'] and isinstance(
                            global_data['npmRepo']['external'], str):
                        if 'external' not in service_info:
                            service_info['external'] = global_data['npmRepo']['external']

                    if 'internal' in global_data['npmRepo'] and global_data['npmRepo']['internal'] and isinstance(
                            global_data['npmRepo']['internal'], str):
                        if 'internal' not in service_info:
                            service_info['internal'] = global_data['npmRepo']['internal']

                if 'nodeLabel' in global_data and isinstance(global_data['nodeLabel'], dict):
                    if 'buildLabel' in global_data['nodeLabel'] and global_data['nodeLabel'][
                        'buildLabel'] and isinstance(global_data['nodeLabel']['buildLabel'], str):
                        if 'buildLabel' not in service_info:
                            service_info['buildLabel'] = global_data['nodeLabel']['buildLabel']

                if 'nodeLabel' in global_data and isinstance(global_data['nodeLabel'], dict):
                    if 'checkoutLabel' in global_data['nodeLabel'] and global_data['nodeLabel'][
                        'checkoutLabel'] and isinstance(global_data['nodeLabel']['checkoutLabel'], str):
                        if 'checkoutLabel' not in service_info:
                            service_info['checkoutLabel'] = global_data['nodeLabel']['checkoutLabel']

                if 'nodeLabel' in global_data and isinstance(global_data['nodeLabel'], dict):
                    if 'compileLabel' in global_data['nodeLabel'] and global_data['nodeLabel'][
                        'compileLabel'] and isinstance(global_data['nodeLabel']['compileLabel'], str):
                        if 'compileLabel' not in service_info:
                            service_info['compileLabel'] = global_data['nodeLabel']['compileLabel']

                if 'nodeLabel' in global_data and isinstance(global_data['nodeLabel'], dict):
                    if 'healCheckLabel' in global_data['nodeLabel'] and global_data['nodeLabel'][
                        'healCheckLabel'] and isinstance(global_data['nodeLabel']['healCheckLabel'], str):
                        if 'healCheckLabel' not in service_info:
                            service_info['healCheckLabel'] = global_data['nodeLabel']['healCheckLabel']

                if 'nodeLabel' in global_data and isinstance(global_data['nodeLabel'], dict):
                    if 'updateLabel' in global_data['nodeLabel'] and global_data['nodeLabel'][
                        'updateLabel'] and isinstance(global_data['nodeLabel']['updateLabel'], str):
                        if 'updateLabel' not in service_info:
                            service_info['updateLabel'] = global_data['nodeLabel']['updateLabel']
                result[namespace['namespace']]['services'].append(service_info)
    #pprint.pprint(result)
    return result




def parse_option():
    option_parser = optparse.OptionParser(usage="Jenkins流水线渲染工具")
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
    render_template(parse_yarm_data(parse_yaml(configFile)))


parse_option()
