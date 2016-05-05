#!/usr/bin/env bash

# Simple script for deploying app.

# Clearing output directory
rm -r deploy/output
mkdir deploy/output
# Generating MD5 hash sum of files that will be added after you install game (for .deb package)
md5deep -r deploy/input/usr deploy/input/opt > deploy/input/DEBIAN/md5sums

# Building project and copying built files to input directory
ant build
rm -r deploy/input/opt/Grid-Stepper-Game/app deploy/input/opt/Grid-Stepper-Game/resources
cp -r build/jar/* deploy/input/opt/Grid-Stepper-Game
rm deploy/input/opt/Grid-Stepper-Game/run.bat deploy/input/opt/Grid-Stepper-Game/run.sh

# Getting build's and package's version
PACKAGE_VERSION=$((`cat ./deploy/.package_version` + 1))
echo ${PACKAGE_VERSION} > deploy/.package_version
# Variable with location of "version-util.py" script
VER_UTIL=./build-info/version-util.py
BUILD_VERSION=`${VER_UTIL} global get`.`${VER_UTIL} commit get`.`${VER_UTIL} build get`

OUTPUT_FILE="deploy/output/grid-stepper-game_${BUILD_VERSION}-${PACKAGE_VERSION}_all.deb"

# Running pre-build configurator
deploy/deb-building-utils/pre_build_config deploy/input ${BUILD_VERSION} ${PACKAGE_VERSION}

# Copying license's text (file LICENSE) to deploy/input/DEBIAN/copyright
cp LICENSE deploy/input/DEBIAN/copyright

# Generating .deb package
fakeroot dpkg-deb --build deploy/input ${OUTPUT_FILE}
git add deploy/output/grid-stepper-game_1.39.12-10_all.deb
