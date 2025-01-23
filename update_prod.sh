#!/bin/bash

#该脚本用来更新生产环境 jenkinsfile
#hu_projects='ipmp olc faap pis merit-award mdm-pmp'
hu_projects='mdm-pmp zcb-versatile hr-versatile ipmp faap merit-award olc pis'
#hu_projects='mdm-versatile'
env='production'


for project in `echo $hu_projects`
do
   ./render -i ./configure/$env/${project}.yaml
done




