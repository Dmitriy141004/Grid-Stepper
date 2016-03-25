#!/usr/bin/env python

# Python script for incrementing build version
# Info about build version is stored in file "./.build_version". It looks like this:
#
# 1.23.40
#
# Description for numbers:
# 1. GLOBAL VERSION. It's number of realize with many differences.
# 2. COMMIT VERSION. It's number of "git commit"s.
# 3. BUILD VERSION. After each launch of Ant build script this number increments by one.

import os
import posixpath as file_sys
from sys import argv as script_args
import re

if len(script_args) < 2:
    print """Missing mode argument!
Acceptable modes:
\tcommit|COMMIT
\tbuild|BUILD
\tglobal|GLOBAL"""
    exit(1)

# Getting directory, where is this file
thisFilePath = os.path.realpath(__file__)
thisFilePath = file_sys.dirname(thisFilePath)

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
    print "Content of file \"" + file_sys.join(thisFilePath, ".build_version") + "\" doesn't pattern " \
                 "\"<global_version>.<commit number>.<build number>\"!"
    exit(1)
# Getting each number
globalVersion = int(parsedBuildInfo.group(1))
commitNumber = int(parsedBuildInfo.group(2))
buildNumber = int(parsedBuildInfo.group(3))

# Switch-like construction for adding or "zeroing" numbers
if script_args[1] == "build" or script_args[1] == "BUILD":
    buildNumber += 1
elif script_args[1] == "commit" or script_args[1] == "COMMIT":
    # Each commit resets build number
    buildNumber = 0
    commitNumber += 1
elif script_args[1] == "global" or script_args[1] == "GLOBAL":
    # Each global change resets commit and build numbers
    buildNumber = 0
    commitNumber = 0
    globalVersion += 1
else:
    # For unknown mode
    print "Unknown mode \"" + script_args[1] + "\"!"
    buildFile.close()
    exit(1)

# Writing and closing file
buildFile.write(str(globalVersion) + "." + str(commitNumber) + "." + str(buildNumber))
buildFile.close()
