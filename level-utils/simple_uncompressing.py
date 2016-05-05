# Generates XML level's game field from "Compressed Level Store Format"
# Example:
#
# wwwwwwwweeww
# wwwwwwweeeew
# wweewweewwee
# weeeeweeewee
# sewweewweeew
# eeweeeweeeew
# wweewweewwee
# wweeweeeewee
# wwweeewfeeew
# wwwwwwwweeww
#
# Also, ignores newlines
#
# Usage:
#     simple_uncompressing.py [INPUT_FILE] [OUTPUT_FILE]
#
# INPUT_FILE       required
# OUTPUT_FILE      if not specified - result will be printed to stdout

import sys

if len(sys.argv) == 1 or '--help' in sys.argv:
    print """
Generates XML level's game field from "Compressed Level Store Format"
Example:

wwwwwwwweeww
wwwwwwweeeew
wweewweewwee
weeeeweeewee
sewweewweeew
eeweeeweeeew
wweewweewwee
wweeweeeewee
wwweeewfeeew
wwwwwwwweeww

Also, ignores newlines

Usage:
    %s [INPUT_FILE] [OUTPUT_FILE]

INPUT_FILE       required
OUTPUT_FILE      if not specified - result will be printed to stdout""" % sys.argv[0]
    exit(0)

if len(sys.argv) < 2:
    print '%s: Missing input file argument!' % sys.argv[0]
    exit(1)

inputFile = open(sys.argv[1], 'r')
outputFile = sys.stdout if len(sys.argv) < 3 else open(sys.argv[2], 'w')

inputData = inputFile.read()
inputFile.close()

waitingForNewColumn = True

for c in inputData:
    if c != ' ':
        if (c == '\n' or c == '\r') and not waitingForNewColumn:
            waitingForNewColumn = True
            outputFile.write('</column>\n')
        else:
            if waitingForNewColumn:
                waitingForNewColumn = False
                outputFile.write('<column>\n')

            tagName = ''
            if c == 'w':
                tagName = 'wall'
            elif c == 'e':
                tagName = 'empty'
            elif c == 's':
                tagName = 'start'
            elif c == 'f':
                tagName = 'finish'

            outputFile.write('\t<%-11s/>\n' % tagName)

if outputFile != sys.stdout:
    outputFile.close()
