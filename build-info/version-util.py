#!/usr/bin/env python

# =================== Python script for incrementing build version ===================
# Info about build version is stored in file "./.build_version". It looks like this:
#
# 1.23.40
#
# Description for numbers:
# 1. GLOBAL VERSION. It's number of realize with many differences.
# 2. COMMIT VERSION. It's number of "git commit"s.
# 3. BUILD VERSION. After each launch of Ant build script this number increments by one.
#
# ======================== Help (--help argument prints this) ========================
# Usage:
#     version-util.py [SELECTOR] [MODE]
#
# SELECTOR-s are:
#     commit
#     build
#     global
#
# MODE-s are:
#     update          increment this selector (number) and zero next numbers at the right
#     increment       increment only this selector
#     zero            zero this selector

import os
import posixpath as file_sys
from sys import argv as script_args
import re

# Printing help if there's --help argument in available
if "--help" in script_args:
    print """
Usage:
    version-util.py [SELECTOR] [MODE]

SELECTOR-s are:
    commit
    build
    global

MODE-s are:
    update          increment this selector (number) and zero next numbers at the right
    increment       increment only this selector
    zero            zero this selector
"""
    exit(0)

if len(script_args) < 3:
    print "Missing mode argument!\nType \"--help\" to get help"
    exit(1)

# If-s for unknown SELECTOR-s and MODE-s
if script_args[1] not in ["global", "commit", "build"]:
    print "Unknown SELECTOR \"" + script_args[1] + "\"!\nType \"--help\" to get help"
    exit(1)

if script_args[2] not in ["update", "increment", "zero"]:
    print "Unknown MODE \"" + script_args[2] + "\"!\nType \"--help\" to get help"
    exit(1)

# Getting directory, where is this file
thisFilePath = os.path.realpath(__file__)
thisFilePath = file_sys.dirname(thisFilePath)

# Loading data from stopper-file
stopperFile = open(file_sys.join(thisFilePath, ".stop_counter"))
if stopperFile.read(1) == "1":
    stopperFile.close()
    exit(0)
stopperFile.close()

# Opening file to read
buildFile = open(file_sys.join(thisFilePath, ".build_version"), 'r')
buildTextInfo = buildFile.read()
# If there's no info about build - set to initial value
if buildTextInfo == "":
    buildTextInfo = "0.0.0"
buildFile.close()

# Opening file for writing
buildFile = open(file_sys.join(thisFilePath, ".build_version"), 'w')
# Getting match of regexp that searches all three numbers
parsedBuildInfo = re.match("""(\\d+)\\.      # Global version
                              (\\d+)\\.      # Commit number
                              (\\d+)         # Build number""", buildTextInfo, re.VERBOSE)
# If there's no match - it's error!
if parsedBuildInfo is None:
    print "Content of file \"" + file_sys.join(thisFilePath, ".build_version") + """\" doesn't match pattern
<global_version>.<commit number>.<build number>!"""
    exit(1)
# Getting each number
globalVersion = int(parsedBuildInfo.group(1))
commitNumber = int(parsedBuildInfo.group(2))
buildNumber = int(parsedBuildInfo.group(3))

if script_args[1] == "build":
    if script_args[2] == "update" or script_args[2] == "increment":
        buildNumber += 1
    elif script_args[2] == "zero":
        buildNumber = 0

elif script_args[1] == "commit":
    if script_args[2] == "update":
        buildNumber = 0
        commitNumber += 1
    elif script_args[2] == "increment":
        commitNumber += 1
    elif script_args[2] == "zero":
        commitNumber = 0

elif script_args[1] == "global":
    if script_args[2] == "update":
        globalVersion += 1
        commitNumber = 0
        buildNumber = 0
    elif script_args[2] == "increment":
        globalVersion += 1
    elif script_args[2] == "zero":
        globalVersion = 0

# Writing and closing file
buildFile.write(str(globalVersion) + "." + str(commitNumber) + "." + str(buildNumber))
buildFile.close()
