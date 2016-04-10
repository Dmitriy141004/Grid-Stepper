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

# Variable with location of "version-util.py" script
VER_UTIL=./build-info/version-util.py
OUTPUT_FILE="deploy/output/Grid-Stepper-Game_`${VER_UTIL} global get`.`${VER_UTIL} commit get`.`${VER_UTIL} build get`-1_all.deb"

# Generating .deb package
fakeroot dpkg-deb --build deploy/input ${OUTPUT_FILE}
