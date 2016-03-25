#!/usr/bin/env bash

# Small script for incrementing build number

cd `pwd`/`dirname $0`

if [ ! -f .build_num ] ; then            # If file not exists, we must create it
    touch .build_num
    cd ../                               # Adding file to git
    git -c core.quotepath=false add -- build-info/.build_num
    cd build-info
fi

BUILD_NUM=`cat .build_num`               # Getting build number
if [ "${BUILD_NUM}" == "" ] ; then       # If number is empty "string", it is set to zero
    BUILD_NUM=0
fi
BUILD_NUM=$((BUILD_NUM+1))               # Incrementing build number

echo ${BUILD_NUM} > .build_num           # Writing to file
