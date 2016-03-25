#!/usr/bin/env python

# Small script for incrementing build version

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

thisFilePath = os.path.realpath(__file__)
thisFilePath = file_sys.dirname(thisFilePath)

buildFile = open(file_sys.join(thisFilePath, ".build_version"), 'r')
buildTextInfo = buildFile.read()
if buildTextInfo == "":
    buildTextInfo = "0.0"
buildFile.close()

buildFile = open(file_sys.join(thisFilePath, ".build_version"), 'w')

parsedBuildInfo = re.match("""(\\d+)\\.      # Global version
                              (\\d+)\\.      # Commit number
                              (\\d+)         # Build number""", buildTextInfo, re.VERBOSE)
if parsedBuildInfo is None:
    print "Content of file \"" + file_sys.join(thisFilePath, ".build_version") + "\" doesn't pattern " \
                 "\"<global_version>.<commit number>.<build number>\"!"
    exit(1)

globalVersion = int(parsedBuildInfo.group(1))
commitNumber = int(parsedBuildInfo.group(2))
buildNumber = int(parsedBuildInfo.group(3))

if script_args[1] == "build" or script_args[1] == "BUILD":
    buildNumber += 1
elif script_args[1] == "commit" or script_args[1] == "COMMIT":
    commitNumber += 1
elif script_args[1] == "global" or script_args[1] == "GLOBAL":
    globalVersion += 1
else:
    print "Unknown mode \"" + script_args[1] + "\"!"
    buildFile.close()
    exit(1)

buildFile.write(str(globalVersion) + "." + str(commitNumber) + "." + str(buildNumber))
buildFile.close()
