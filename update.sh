#!/bin/bash

#该脚本用来更新jenkinsfile
#hu_projects='ipmp olc faap pis merit-award mdm-pmp'
hu_projects='ipmp olc faap pis merit-award mdm-pmp mdm-versatile'
#hu_projects='ipmp'
env_list='test dev'
#env_list='test'


for project in `echo $hu_projects`
do
   for env in  `echo $env_list`
   do
          ./render -i ./configure/$env/${project}.yaml
   done
done




